package me.cat.toybox.items.portal;

import me.cat.toybox.helpers.LoopHelper;
import org.bukkit.Location;
import org.bukkit.Material;
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
            if (!player.isValid() || !testSubject.isValid()) {
                if (testSubject.isValid()) {
                    testSubject.remove();
                }
                task.cancel();
            }
            Location playerLoc = player.getLocation();
            Location modLoc = playerLoc.clone().add(playerLoc.getDirection().multiply(PortalItem.DISTANCE_BETWEEN));
            modLoc.setYaw(playerLoc.getYaw() - 180f);
//                        modLoc.add(0.0d, 0.5d, 0.0d);

            double sustainedY;
            Block blockBelowEntity = modLoc.clone()
                    .subtract(0.0d, 0.5d, 0.0d)
                    .getBlock();
            if (blockBelowEntity.getType() != Material.AIR) {
                sustainedY = playerLoc.y();
            } else {
                sustainedY = modLoc.y();
            }
            modLoc.setY(sustainedY);

            boolean solidity = false;
            for (double x = -0.5d; x <= 0.5; x += 0.5) {
                for (double y = 0.0d; y <= 1.0d; y += 0.5d) {
                    for (double z = -0.5d; z <= 0.5d; z += 0.5d) {
                        Block blockAround = modLoc.clone().add(x, y, z).getBlock();
                        if (blockAround.getType().isSolid()) {
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
                int highestYAround = testSubject.getWorld().getHighestBlockYAt(modLoc.getBlockX(), modLoc.getBlockZ());
                modLoc.setY(highestYAround + 1.0d);
            }

//                entity.teleport(modLoc.clone().add(0.0d, 0.5d, 0.0d));
            testSubject.teleport(modLoc);
        });
    }

    private void toggleEntityAttributes(Entity entity, boolean toggle) {
        entity.setInvulnerable(toggle);
        entity.setGravity(!toggle);
        entity.setSilent(toggle);
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
