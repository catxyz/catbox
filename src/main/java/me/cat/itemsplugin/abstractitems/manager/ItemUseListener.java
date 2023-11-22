package me.cat.itemsplugin.abstractitems.manager;

import me.cat.itemsplugin.Helper;
import me.cat.itemsplugin.ItemsPlugin;
import me.cat.itemsplugin.abstractitems.abstraction.AbstractItem;
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

public class ItemUseListener implements Listener {

    private final AbstractItemManager abstractItemManager;
    private CooldownManager cooldownManager;

    public ItemUseListener(AbstractItemManager abstractItemManager) {
        this.abstractItemManager = abstractItemManager;
        this.cooldownManager = ItemsPlugin.getInstance().getCooldownManager();
    }

    @EventHandler
    public void onAbstractItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action currentAction = event.getAction();

        ItemStack currentItem = event.getItem();
        if (currentItem == null) {
            return;
        }

        PersistentDataContainer pdc = currentItem.getItemMeta()
                .getPersistentDataContainer();
        if (pdc.has(AbstractItem.IS_CUSTOM_ITEM_TAG) && pdc.has(AbstractItem.CUSTOM_ITEM_ID_TAG)) {
            Boolean isCustomItem = pdc.get(AbstractItem.IS_CUSTOM_ITEM_TAG, PersistentDataType.BOOLEAN);
            if (Boolean.TRUE.equals(isCustomItem)) {
                String currentItemId = pdc.get(AbstractItem.CUSTOM_ITEM_ID_TAG, PersistentDataType.STRING);

                Optional<AbstractItem> abstractItem = abstractItemManager.getItemById(currentItemId);
                abstractItem.ifPresentOrElse(item -> {
                    AbstractItem.AbstractItemBuilder builder = item.getBuilder();

                    if (builder.getUseActions().contains(currentAction)) {
                        if (builder.getMaterial() == currentItem.getType()) {
                            if (Objects.equals(builder.getItemId(), currentItemId)) {
                                if (ItemsPlugin.getInstance().abilitiesDisabled()) {
                                    player.sendMessage(Component.text("This ability is currently disabled!", NamedTextColor.RED));
                                    event.setCancelled(true);
                                    return;
                                }

                                if (cooldownManager.isCooldownOver(player.getUniqueId(), builder.getUseCooldown())) {
                                    player.sendMessage(Helper.getPlayActivatedMessageComponent(builder.getDisplayName()));
                                    item.useItemInteraction(event);

                                    cooldownManager.removeFromCooldown(player.getUniqueId());
                                    cooldownManager.addToCooldown(player.getUniqueId());
                                } else {
                                    player.sendMessage(Helper.getPlayCooldownMessageComponent(builder.getDisplayName(), builder.getUseCooldown()));
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }, () -> player.sendMessage(Component.text("This item wasn't found!", NamedTextColor.RED)));
            }
        }
    }
}
