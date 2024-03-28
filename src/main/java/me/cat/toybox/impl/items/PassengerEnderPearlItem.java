package me.cat.toybox.impl.items;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import me.cat.toybox.ToyboxPlugin;
import me.cat.toybox.helpers.Helper;
import me.cat.toybox.impl.abstraction.AbstractItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PassengerEnderPearlItem extends AbstractItem implements Listener {

    private static final String PASSENGER_ENDER_PEARL_ITEM_ID = "passenger_ender_pearl";
    private static final NamespacedKey PASSENGER_ENDER_PEARL_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "passenger_ender_pearl",
            ToyboxPlugin.getInstance()
    ));
    private static final long DESPAWN_SECONDS = 30L;
    private static final int OFFHAND_SLOT_NUMBER = 40;
    private static final List<EnderPearl> ACTIVE_ENDER_PEARLS = Lists.newArrayList();

    public PassengerEnderPearlItem() {
        super(
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .setItemId(PASSENGER_ENDER_PEARL_ITEM_ID)
                        .setMaterial(Material.ENDER_PEARL)
                        .addData(PASSENGER_ENDER_PEARL_TAG, PersistentDataType.BOOLEAN, true)
                        .setDisplayName(Component.text("Passenger Ender Pearl", NamedTextColor.YELLOW))
                        .setLore(List.of(
                                Component.empty(),
                                Component.text("???", NamedTextColor.GRAY)
                        ))
        );
    }

    @Override
    public void initAdditionalItemData() {
        runFallbackOffHandEnderPearlChecker();
    }

    @Override
    public void useItemInteraction(PlayerInteractEvent event) {
    }

    @EventHandler
    public void onPlayerLaunchProjectile(PlayerLaunchProjectileEvent event) {
        Player player = event.getPlayer();
        ItemStack enderPearl = event.getItemStack();

        if (enderPearl.getItemMeta().getPersistentDataContainer().has(PASSENGER_ENDER_PEARL_TAG)) {
            event.setShouldConsume(false);

            EnderPearl enderPearlEntity = (EnderPearl) event.getProjectile();
            enderPearlEntity.getPersistentDataContainer()
                    .set(PASSENGER_ENDER_PEARL_TAG, PersistentDataType.BOOLEAN, true);
            enderPearlEntity.addPassenger(player);
            ACTIVE_ENDER_PEARLS.add(enderPearlEntity);

            AtomicInteger ticksPassed = new AtomicInteger();
            AtomicInteger enderPearlSecondsAlive = new AtomicInteger();

            Bukkit.getServer().getScheduler().runTaskTimer(ToyboxPlugin.getInstance(), (task) -> {
                ticksPassed.getAndIncrement();
                if (ticksPassed.get() % 20 == 0) {
                    enderPearlSecondsAlive.getAndIncrement();
                }

                if (!enderPearlEntity.isValid()) {
                    task.cancel();
                }

                if (enderPearlSecondsAlive.get() >= DESPAWN_SECONDS) {
                    Helper.removeEntitiesInStyle(Particle.SONIC_BOOM, 1, enderPearlEntity);
                    task.cancel();
                }

                ACTIVE_ENDER_PEARLS.stream()
                        .filter(activeEnderPearlEntity -> activeEnderPearlEntity.getPassengers().isEmpty())
                        .forEach(EnderPearl::remove);

                if (ACTIVE_ENDER_PEARLS.isEmpty()) {
                    task.cancel();
                }
            }, 0L, 1L);
        }
    }

    @EventHandler
    public void onProjectileLand(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof EnderPearl enderPearl) {
            if (enderPearl.getPersistentDataContainer().has(PASSENGER_ENDER_PEARL_TAG)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onOffhandSwitch(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack offHandItem = event.getOffHandItem();

        if (offHandItem.getItemMeta().getPersistentDataContainer().has(PASSENGER_ENDER_PEARL_TAG)) {
            player.sendMessage(Component.text("Can't swap this item!", NamedTextColor.RED));
            event.setCancelled(true);
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
                    if (itemOnCursor.getItemMeta().getPersistentDataContainer().has(PASSENGER_ENDER_PEARL_TAG)) {
                        player.sendMessage(Component.text("Can't do that!", NamedTextColor.RED));
                        event.setCancelled(true);

                        player.getOpenInventory().setCursor(null);
                        player.getInventory().addItem(getSelfItemStack());
                    }
                }
            }
        }
    }

    private void runFallbackOffHandEnderPearlChecker() {
        Bukkit.getServer().getScheduler().runTaskTimer(ToyboxPlugin.getInstance(), (task) -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                PlayerInventory playerInventory = player.getInventory();
                ItemStack offHandItem = playerInventory.getItemInOffHand();

                if (offHandItem.getType() != Material.AIR) {
                    if (offHandItem.getItemMeta().getPersistentDataContainer().has(PASSENGER_ENDER_PEARL_TAG)) {
                        playerInventory.setItemInOffHand(null);
                        playerInventory.addItem(getSelfItemStack());
                    }
                }
            }
        }, 0L, 5L);
    }
}
