package me.cat.toybox.impl.managers;

import me.cat.toybox.ToyboxPlugin;
import me.cat.toybox.helpers.Helper;
import me.cat.toybox.impl.abstraction.item.ToyboxItem;
import me.cat.toybox.impl.abstraction.item.ToyboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ItemUseListener implements Listener {

    private final ToyboxItemManager toyboxItemManager;
    private final CooldownManager cooldownManager;

    public ItemUseListener(ToyboxItemManager toyboxItemManager,
                           CooldownManager cooldownManager) {
        this.toyboxItemManager = toyboxItemManager;
        this.cooldownManager = cooldownManager;
    }

    @EventHandler
    public void onToyboxItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action currentAction = event.getAction();

        ItemStack currentItem = event.getItem();
        if (currentItem == null) {
            return;
        }

        PersistentDataContainer pdc = currentItem.getItemMeta()
                .getPersistentDataContainer();

        if (pdc.has(ToyboxItem.IS_CUSTOM_ITEM_TAG) && pdc.has(ToyboxItem.CUSTOM_ITEM_ID_TAG)) {
            Boolean isCustomItem = pdc.get(ToyboxItem.IS_CUSTOM_ITEM_TAG, PersistentDataType.BOOLEAN);

            if (Boolean.TRUE.equals(isCustomItem)) {
                String currentItemId = pdc.get(ToyboxItem.CUSTOM_ITEM_ID_TAG, PersistentDataType.STRING);

                Optional<ToyboxItem> toyboxItem = toyboxItemManager.getItemById(currentItemId);
                toyboxItem.ifPresentOrElse(item -> {
                    ToyboxItemBuilder builder = item.builder();

                    if (builder.storedUseActions().contains(currentAction)) {
                        if (builder.material() == currentItem.getType()) {
                            if (Objects.equals(builder.itemId(), currentItemId)) {
                                if (areAbilitiesDisabled(player, event)) {
                                    return;
                                }

                                UUID playerId = player.getUniqueId();
                                if (cooldownManager.isCooldownOver(playerId, builder.useCooldown())) {
                                    player.sendMessage(Helper.getActivatedMessageComponent(builder.displayName()));
                                    item.onUse(event);
                                    event.setCancelled(builder.shouldCancelUseInteraction());

                                    cooldownManager.removeFromCooldown(playerId);
                                    cooldownManager.addToCooldown(playerId);
                                } else {
                                    player.sendMessage(Helper.getCooldownMessageComponent(builder.displayName(), builder.useCooldown()));
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }, () -> player.sendMessage(Component.text("This item wasn't found!", NamedTextColor.RED)));
            }
        }
    }

    private boolean areAbilitiesDisabled(Player player, PlayerInteractEvent event) {
        boolean abilitiesDisabled = ToyboxPlugin.get().abilitiesDisabled();

        if (abilitiesDisabled) {
            player.sendMessage(Component.text("This ability is currently disabled!", NamedTextColor.RED));
            event.setCancelled(true);
        }
        return abilitiesDisabled;
    }
}
