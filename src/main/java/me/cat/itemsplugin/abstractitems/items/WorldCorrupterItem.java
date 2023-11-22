package me.cat.itemsplugin.abstractitems.items;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.google.common.base.Preconditions;
import me.cat.itemsplugin.Helper;
import me.cat.itemsplugin.ItemsPlugin;
import me.cat.itemsplugin.abstractitems.abstraction.AbstractItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class WorldCorrupterItem extends AbstractItem implements Listener {

    private static final NamespacedKey WORLD_CORRUPTER_TRIDENT_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "world_corrupter_trident",
            ItemsPlugin.getInstance()
    ));

    public WorldCorrupterItem() {
        super(
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .setItemId("world_corrupter")
                        .setUseCooldown(Duration.ZERO)
                        .setMaterial(Material.TRIDENT)
                        .setEnchants(Map.of(
                                Enchantment.LOYALTY, 5
                        ))
                        .addData(WORLD_CORRUPTER_TRIDENT_TAG, PersistentDataType.BOOLEAN, true)
                        .setItemFlags(List.of(
                                ItemFlag.HIDE_ENCHANTS,
                                ItemFlag.HIDE_ATTRIBUTES
                        ))
                        .setDisplayName(Component.text("World Corrupter", NamedTextColor.RED))
                        .setLore(List.of(
                                Component.text("???", NamedTextColor.GRAY)
                        ))
        );
    }

    @Override
    public void useItemInteraction(PlayerInteractEvent event) {
    }

    @EventHandler
    public void onPlayerLaunchProjectile(PlayerLaunchProjectileEvent event) {
        ItemStack trident = event.getItemStack();
        if (trident.getItemMeta().getPersistentDataContainer().has(WORLD_CORRUPTER_TRIDENT_TAG)) {
            Trident tridentEntity = (Trident) event.getProjectile();

            Bukkit.getServer().getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (task) -> {
                if (tridentEntity.isOnGround()) {
                    Helper.createSurfaceLayer(
                            tridentEntity.getWorld(),
                            tridentEntity.getLocation(),
                            ThreadLocalRandom.current().nextInt(5, 10),
                            Arrays.stream(Material.values()).toList(),
                            affectedBlocks -> affectedBlocks.forEach(block -> tridentEntity.getWorld().strikeLightningEffect(block.getLocation()))
                    );
                    task.cancel();
                }
            }, 0L, 1L);
        }
    }
}