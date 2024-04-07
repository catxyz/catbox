package me.cat.toybox.items.portal;

import com.google.common.collect.Maps;
import me.cat.toybox.helpers.LoopHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitTask;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PortalListener implements Listener {

    private final Map<UUID, UUID> mappedPlayerAndEntity;
    private final Map<UUID, BukkitTask> mappedEntityAndTask;

    public PortalListener() {
        this.mappedPlayerAndEntity = Maps.newHashMap();
        this.mappedEntityAndTask = Maps.newHashMap();
    }

    @EventHandler
    public void onEntityRightClick(PlayerInteractAtEntityEvent event) {
        EquipmentSlot hand = event.getHand();
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        ItemStack playerHandItem = getHeldItem(player, hand);
        ItemMeta handItemMeta = playerHandItem.getItemMeta();
        if (handItemMeta != null) {
            PersistentDataContainer handItemPdc = handItemMeta.getPersistentDataContainer();

            if (handItemPdc.has(PortalItem.IS_PORTAL_DEVICE_TAG)) {
                String entityName = entity.getName();

                if (!isEntityAlreadyControlled(entity)) {
                    player.sendMessage(Component.text("You are now controlling ", NamedTextColor.GRAY)
                            .append(getEntityName(entity))
                            .append(Component.text('!', NamedTextColor.GRAY)));
                    pickUpEntity(player, entity);
                } else {
                    if (!isPlayerControllerOfEntity(player, entity)) {
                        player.sendMessage(Component.text(entityName, NamedTextColor.YELLOW)
                                .append(Component.text(" is not yours!", NamedTextColor.RED)));
                        return;
                    }

                    player.sendMessage(Component.text("No longer controlling ", NamedTextColor.RED)
                            .append(getEntityName(entity))
                            .append(Component.text('!', NamedTextColor.RED)));
                    dropEntity(player, entity);
                }
            }
        }
    }

    @EventHandler
    public void onEntityMount(EntityMountEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerInventory playerInventory = player.getInventory();
            ItemStack playerHandItem;

            if (!playerInventory.getItemInMainHand().isEmpty()) {
                playerHandItem = playerInventory.getItemInMainHand();
            } else if (!playerInventory.getItemInOffHand().isEmpty()) {
                playerHandItem = playerInventory.getItemInOffHand();
            } else {
                playerHandItem = null;
            }

            if (playerHandItem != null) {
                PersistentDataContainer handItemPdc = playerHandItem.getItemMeta()
                        .getPersistentDataContainer();

                if (handItemPdc.has(PortalItem.IS_PORTAL_DEVICE_TAG)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        UUID entityId = mappedPlayerAndEntity.get(playerId);

        if (entityId != null) {
            Entity entity = Bukkit.getServer().getEntity(entityId);
            toggleEntityAttributes(entity, false);
        }
        mappedPlayerAndEntity.remove(playerId);
    }

    public void pickUpEntity(Player player, Entity entity) {
        UUID entityId = entity.getUniqueId();

        mappedPlayerAndEntity.putIfAbsent(player.getUniqueId(), entityId);
        toggleEntityAttributes(entity, true);

        LoopHelper.runIndefinitely(0L, 1L, (task) -> {
            mappedEntityAndTask.putIfAbsent(entityId, task);
            handleTaskPersistence(player, entity);

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
                int highestYAround = entity.getWorld()
                        .getHighestBlockYAt(modLoc.getBlockX(), modLoc.getBlockZ());
                modLoc.setY(highestYAround + 1.0);
            }

            entity.teleport(modLoc.clone().add(0, 0.5, 0));
        });
    }

    public void dropEntity(Player player, Entity entity) {
        if (player == null || entity == null) {
            return;
        }
        if (!player.isValid() || !entity.isValid()) {
            return;
        }
        UUID playerId = player.getUniqueId();
        UUID entityId = entity.getUniqueId();

        BukkitTask hookedTask = mappedEntityAndTask.get(entityId);
        if (hookedTask != null) {
            hookedTask.cancel();
        }
        toggleEntityAttributes(entity, false);

        mappedEntityAndTask.remove(entityId);
        mappedPlayerAndEntity.remove(playerId);
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

    private Component getEntityName(Entity entity) {
        Component entityCustomName = entity.customName();

        if (entityCustomName != null) {
            boolean hasColor = entityCustomName.hasStyling();
            if (hasColor) {
                return entityCustomName;
            } else {
                return entityCustomName.color(NamedTextColor.YELLOW);
            }
        } else {
            return Component.text(entity.getName(), NamedTextColor.YELLOW);
        }
    }

    private boolean isEntityAlreadyControlled(Entity entity) {
        return mappedPlayerAndEntity.containsValue(entity.getUniqueId());
    }

    private boolean isPlayerControllerOfEntity(Player player, Entity entity) {
        UUID playerId = player.getUniqueId();
        UUID entityId = entity.getUniqueId();

        if (!mappedPlayerAndEntity.containsValue(entityId)) {
            return false;
        }
        UUID storedEntityId = mappedPlayerAndEntity.get(playerId);
        return Objects.equals(entityId, storedEntityId);
    }

    private void toggleEntityAttributes(Entity entity, boolean toggle) {
        if (entity != null) {
            entity.setGravity(!toggle);
            entity.setSilent(toggle);
            entity.setGlowing(toggle);
        }
    }

    private void handleTaskPersistence(Player player, Entity entity) {
        if (!player.isValid()) {
            dropEntity(player, entity);
        }
        if (!entity.isValid()) {
            dropEntity(player, entity);
        }
    }
}
