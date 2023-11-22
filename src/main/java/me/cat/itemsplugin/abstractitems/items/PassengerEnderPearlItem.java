package me.cat.itemsplugin.abstractitems.items;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import me.cat.itemsplugin.ItemsPlugin;
import me.cat.itemsplugin.abstractitems.abstraction.AbstractItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class PassengerEnderPearlItem extends AbstractItem implements Listener {

    private static final String PASSENGER_ENDER_PEARL_ITEM_ID = "passenger_ender_pearl";
    private static final NamespacedKey PASSENGER_ENDER_PEARL_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "passenger_ender_pearl",
            ItemsPlugin.getInstance()
    ));
    private static final long DESPAWN_SECONDS = 10L;
    private static final int OFFHAND_SLOT_NUMBER = 40;
    private Player thrower;
    private final List<EnderPearl> activeEnderPearls;

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
                                Component.text("???", NamedTextColor.GRAY)
                        ))
        );

        this.activeEnderPearls = Lists.newArrayList();
        checkForBadEnderPearls();
    }

    @Override
    public void useItemInteraction(PlayerInteractEvent event) {
        this.thrower = event.getPlayer();
    }

    @EventHandler
    public void onPlayerLaunchProjectile(PlayerLaunchProjectileEvent event) {
        Player player = event.getPlayer();
        ItemStack enderPearl = event.getItemStack();

        if (enderPearl.getItemMeta().getPersistentDataContainer().has(PASSENGER_ENDER_PEARL_TAG)) {
            Bukkit.getServer().getScheduler().runTask(ItemsPlugin.getInstance(),
                    () -> player.getInventory().setItemInMainHand(getBuilder().toItemStack()));

            EnderPearl enderPearlEntity = (EnderPearl) event.getProjectile();
            enderPearlEntity.getPersistentDataContainer()
                    .set(PASSENGER_ENDER_PEARL_TAG, PersistentDataType.BOOLEAN, true);
            enderPearlEntity.addPassenger(player);
            activeEnderPearls.add(enderPearlEntity);

            Bukkit.getServer().getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (task) -> {
                activeEnderPearls.stream()
                        .filter(activeEnderPearlEntity -> activeEnderPearlEntity.getPassengers().isEmpty())
                        .forEach(EnderPearl::remove);

                if (activeEnderPearls.isEmpty()) {
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
        if (event.getOffHandItem().getItemMeta().getPersistentDataContainer().has(PASSENGER_ENDER_PEARL_TAG)) {
            event.getPlayer().sendMessage(Component.text("You can't swap this item!", NamedTextColor.RED));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getSlot() == OFFHAND_SLOT_NUMBER) {
            ItemStack itemOnCursor = event.getCursor();
            if (itemOnCursor.getType() == Material.AIR) {
                return;
            }
            if (itemOnCursor.getItemMeta().getPersistentDataContainer().has(PASSENGER_ENDER_PEARL_TAG)) {
                player.sendMessage(Component.text("You can't do that!", NamedTextColor.RED));
                event.setCancelled(true);

                event.setCursor(null);
                player.getInventory().addItem(getBuilder().toItemStack());
            }
        }
    }

    private void checkForBadEnderPearls() {
        Bukkit.getServer().getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (task) -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerInventory playerInventory = player.getInventory();
                ItemStack offHandItem = playerInventory.getItemInOffHand();

                if (offHandItem.getType() != Material.AIR) {
                    if (offHandItem.getItemMeta().getPersistentDataContainer().has(PASSENGER_ENDER_PEARL_TAG)) {
                        playerInventory.setItemInOffHand(null);
                        playerInventory.addItem(getBuilder().toItemStack());
                    }
                }
            }
        }, 0L, 5L);
    }

    public Player getThrower() {
        return thrower;
    }
}
