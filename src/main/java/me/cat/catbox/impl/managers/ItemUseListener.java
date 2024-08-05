package me.cat.catbox.impl.managers;

import me.cat.catbox.CatboxPlugin;
import me.cat.catbox.helpers.MiscHelper;
import me.cat.catbox.impl.abstraction.item.CatboxItem;
import me.cat.catbox.impl.abstraction.item.CatboxItemBuilder;
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

    private final CatboxItemManager catboxItemManager;
    private final CooldownManager cooldownManager;

    public ItemUseListener(CatboxItemManager catboxItemManager,
                           CooldownManager cooldownManager) {
        this.catboxItemManager = catboxItemManager;
        this.cooldownManager = cooldownManager;
    }

    @EventHandler
    public void onCatboxItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action currentAction = event.getAction();

        ItemStack currentItem = event.getItem();
        if (currentItem == null) {
            return;
        }

        PersistentDataContainer pdc = currentItem.getItemMeta()
                .getPersistentDataContainer();

        if (pdc.has(CatboxItem.IS_CUSTOM_ITEM_TAG) && pdc.has(CatboxItem.CUSTOM_ITEM_ID_TAG)) {
            Boolean isCustomItem = pdc.get(CatboxItem.IS_CUSTOM_ITEM_TAG, PersistentDataType.BOOLEAN);

            if (Boolean.TRUE.equals(isCustomItem)) {
                String currentItemId = pdc.get(CatboxItem.CUSTOM_ITEM_ID_TAG, PersistentDataType.STRING);

                Optional<CatboxItem> catboxItem = catboxItemManager.getItemById(currentItemId);
                catboxItem.ifPresentOrElse(item -> {
                    CatboxItemBuilder builder = item.builder();

                    if (builder.storedUseActions().contains(currentAction)) {
                        if (builder.material() == currentItem.getType()) {
                            if (Objects.equals(builder.itemId(), currentItemId)) {
                                if (areAbilitiesDisabled(player, event)) {
                                    return;
                                }

                                UUID playerId = player.getUniqueId();
                                if (cooldownManager.isCooldownOver(playerId, builder.useCooldown())) {
                                    player.sendMessage(MiscHelper.getActivatedMessageComponent(builder.displayName()));
                                    item.onUse(event);
                                    event.setCancelled(builder.shouldCancelUseInteraction());

                                    cooldownManager.removeFromCooldown(playerId);
                                    cooldownManager.addToCooldown(playerId);
                                } else {
                                    player.sendMessage(MiscHelper.getCooldownMessageComponent(builder.displayName(), builder.useCooldown()));
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }, () -> player.sendMessage(Component.text("This is a legacy item!", NamedTextColor.RED)));
            }
        }
    }

    private boolean areAbilitiesDisabled(Player player, PlayerInteractEvent event) {
        boolean abilitiesDisabled = CatboxPlugin.get().abilitiesDisabled();

        if (abilitiesDisabled) {
            player.sendMessage(Component.text("This ability is currently disabled!", NamedTextColor.RED));
            event.setCancelled(true);
        }
        return abilitiesDisabled;
    }
}
