package me.cat.toybox.items.portal;

import me.cat.toybox.helpers.LoopHelper;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitTask;

public class PortalListener implements Listener {

    // todo -> map for a per-player picked up entity? (k, v) -> (player, picked-up entity)
    private Entity testSubject; // todo -> remove this in favor of the per-player entity map thing

    @EventHandler
    public void onEntityRightClick(PlayerInteractAtEntityEvent event) {
        EquipmentSlot hand = event.getHand();
        Player player = event.getPlayer();
        testSubject = event.getRightClicked();

        ItemStack playerHandItem = getHeldItem(player, hand);
        ItemMeta handItemMeta = playerHandItem.getItemMeta();
        if (handItemMeta != null) {
            PersistentDataContainer handItemPdc = handItemMeta.getPersistentDataContainer();

            if (handItemPdc.has(PortalItem.PORTAL_DEVICE_TAG)) {
                pickUpTestSubject(player);
            }
        }
    }

    // todo -> make sure these are using parameters instead of 'testSubject'
    public void pickUpTestSubject(Player player) {
        toggleEntityAttributes(testSubject, true);

        LoopHelper.runIndefinitely(0L, 1L, (task) -> {
            handleTaskPersistence(player, task);

            Location playerLoc = player.getLocation();
            Location modLoc = playerLoc.clone().add(playerLoc.getDirection().multiply(PortalItem.DISTANCE_BETWEEN));
            modLoc.setYaw(playerLoc.getYaw() - 180f);

            boolean solidity = false;
            for (double x = -0.5; x <= 0.5; x += 0.5) {
                for (double y = 0.0; y <= 2.0; y += 0.5) {
                    for (double z = -0.5; z <= 0.5; z += 0.5) {
                        Block blockAround = modLoc.clone()
                                .add(x, y, z)
                                .getBlock();
                        if (blockAround.isSolid()) {
                            solidity = true;
                            break;
                        }
                    }
                    if (solidity) {
                        break;
                    }
                }
                if (solidity) {
                    break;
                }
            }

            if (solidity) {
                int highestYAround = testSubject.getWorld()
                        .getHighestBlockYAt(modLoc.getBlockX(), modLoc.getBlockZ());
                modLoc.setY(highestYAround + 1.0);
            }

            testSubject.teleport(modLoc.clone().add(0, 0.5, 0));
        });
    }

    private void handleTaskPersistence(Player player, BukkitTask hookedTask) {
        if (!player.isValid()) {
            toggleEntityAttributes(testSubject, false);
            hookedTask.cancel();
        }
        if (!testSubject.isValid()) {
            hookedTask.cancel();
        }
    }

    private void toggleEntityAttributes(Entity entity, boolean toggle) {
        entity.setGravity(!toggle);
        entity.setSilent(toggle);
        entity.setGlowing(toggle);
    }

    private ItemStack getHeldItem(Player player, EquipmentSlot hand) {
        ItemStack playerHandItem;
        PlayerInventory playerInventory = player.getInventory();

        if (hand == EquipmentSlot.HAND) {
            playerHandItem = playerInventory.getItemInMainHand();
        } else {
            playerHandItem = playerInventory.getItemInOffHand();
        }
        return playerHandItem;
    }
}
