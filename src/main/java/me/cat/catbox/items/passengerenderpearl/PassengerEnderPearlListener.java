package me.cat.catbox.items.passengerenderpearl;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.google.common.collect.Lists;
import me.cat.catbox.helpers.Helper;
import me.cat.catbox.helpers.LoopHelper;
import me.cat.catbox.impl.abstraction.interfaces.EntityLifetimeLooper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PassengerEnderPearlListener implements Listener, EntityLifetimeLooper {

    private static final int OFFHAND_SLOT_NUMBER = 40;
    private static final List<EnderPearl> ACTIVE_ENDER_PEARLS = Lists.newArrayList();
    private final PassengerEnderPearlItem passengerEnderPearlItem;

    public PassengerEnderPearlListener(PassengerEnderPearlItem passengerEnderPearlItem) {
        this.passengerEnderPearlItem = passengerEnderPearlItem;
    }

    @Override
    public void defineLifetimeFor(Entity... entities) {
        EnderPearl enderPearl = (EnderPearl) entities[0];

        AtomicInteger ticksPassed = new AtomicInteger();
        AtomicInteger enderPearlSecondsAlive = new AtomicInteger();

        LoopHelper.runIndefinitely(0L, 1L, (task) -> {
            ticksPassed.getAndIncrement();
            if (ticksPassed.get() % 20 == 0) {
                enderPearlSecondsAlive.getAndIncrement();
            }

            if (!enderPearl.isValid()) {
                task.cancel();
            }

            if (enderPearlSecondsAlive.get() >= PassengerEnderPearlItem.DESPAWN_SECONDS) {
                Helper.removeEntitiesInStyle(Particle.SONIC_BOOM, 1, enderPearl);
                task.cancel();
            }

            ACTIVE_ENDER_PEARLS.stream()
                    .filter(activeEnderPearlEntity -> activeEnderPearlEntity.getPassengers().isEmpty())
                    .forEach(EnderPearl::remove);

            if (ACTIVE_ENDER_PEARLS.isEmpty()) {
                task.cancel();
            }
        });
    }

    @EventHandler
    public void onPlayerLaunchProjectile(PlayerLaunchProjectileEvent event) {
        Player player = event.getPlayer();
        ItemStack enderPearl = event.getItemStack();

        if (enderPearl.getItemMeta().getPersistentDataContainer().has(PassengerEnderPearlItem.IS_PASSENGER_ENDER_PEARL_TAG)) {
            event.setShouldConsume(false);

            EnderPearl enderPearlEntity = (EnderPearl) event.getProjectile();
            enderPearlEntity.getPersistentDataContainer()
                    .set(PassengerEnderPearlItem.IS_PASSENGER_ENDER_PEARL_TAG, PersistentDataType.BOOLEAN, true);
            enderPearlEntity.addPassenger(player);
            ACTIVE_ENDER_PEARLS.add(enderPearlEntity);

            defineLifetimeFor(enderPearlEntity);
        }
    }

    @EventHandler
    public void onProjectileLand(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof EnderPearl enderPearl) {
            if (enderPearl.getPersistentDataContainer().has(PassengerEnderPearlItem.IS_PASSENGER_ENDER_PEARL_TAG)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onOffhandSwitch(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack offHandItem = event.getOffHandItem();

        if (offHandItem.getType() != Material.AIR) {
            ItemMeta offHandItemMeta = offHandItem.getItemMeta();
            if (offHandItemMeta != null) {
                if (offHandItemMeta.getPersistentDataContainer().has(PassengerEnderPearlItem.IS_PASSENGER_ENDER_PEARL_TAG)) {
                    player.sendMessage(Component.text("Can't swap this item!", NamedTextColor.RED));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory != null) {
            if (clickedInventory.getType() == InventoryType.PLAYER) {
                if (event.getSlot() == OFFHAND_SLOT_NUMBER) {
                    ItemStack itemOnCursor = event.getCursor();
                    if (itemOnCursor.getType() == Material.AIR) {
                        return;
                    }

                    if (itemOnCursor.getItemMeta().getPersistentDataContainer()
                            .has(PassengerEnderPearlItem.IS_PASSENGER_ENDER_PEARL_TAG)) {
                        player.sendMessage(Component.text("Can't do that!", NamedTextColor.RED));
                        event.setCancelled(true);

                        player.getOpenInventory().setCursor(null);
                        player.getInventory().addItem(passengerEnderPearlItem.getSelfItemStack());
                    }
                }
            }
        }
    }

    protected void runFallbackOffHandEnderPearlChecker() {
        LoopHelper.runIndefinitely(0L, 5L, (task) -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                PlayerInventory playerInventory = player.getInventory();
                ItemStack offHandItem = playerInventory.getItemInOffHand();

                if (offHandItem.getType() != Material.AIR) {
                    if (offHandItem.getItemMeta().getPersistentDataContainer()
                            .has(PassengerEnderPearlItem.IS_PASSENGER_ENDER_PEARL_TAG)) {
                        playerInventory.setItemInOffHand(null);
                        playerInventory.addItem(passengerEnderPearlItem.getSelfItemStack());
                    }
                }
            }
        });
    }
}
