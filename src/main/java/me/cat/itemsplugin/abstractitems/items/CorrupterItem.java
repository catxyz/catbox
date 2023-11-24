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

public class CorrupterItem extends AbstractItem implements Listener {

    private static final List<Material> CORRUPTED_MATERIALS;
    private static final NamespacedKey CORRUPTER_TRIDENT_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "corrupter_trident",
            ItemsPlugin.getInstance()
    ));

    static {
        CORRUPTED_MATERIALS = Arrays.stream(Material.values())
                .filter(material -> material.name().endsWith("_WOOL"))
                .toList();
    }

    public CorrupterItem() {
        super(
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .setItemId("corrupter")
                        .setUseCooldown(Duration.ZERO)
                        .setMaterial(Material.TRIDENT)
                        .setEnchants(Map.of(
                                Enchantment.LOYALTY, 5
                        ))
                        .addData(CORRUPTER_TRIDENT_TAG, PersistentDataType.BOOLEAN, true)
                        .setItemFlags(List.of(
                                ItemFlag.HIDE_ENCHANTS,
                                ItemFlag.HIDE_ATTRIBUTES
                        ))
                        .setDisplayName(Component.text("Corrupter", NamedTextColor.YELLOW))
                        .setLore(List.of(
                                Component.empty(),
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
        if (trident.getItemMeta().getPersistentDataContainer().has(CORRUPTER_TRIDENT_TAG)) {
            Trident tridentEntity = (Trident) event.getProjectile();

            Bukkit.getServer().getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (task) -> {
                if (tridentEntity.isOnGround()) {
                    Helper.createSurfaceLayer(
                            tridentEntity.getWorld(),
                            tridentEntity.getLocation(),
                            ThreadLocalRandom.current().nextInt(5, 10),
                            CORRUPTED_MATERIALS,
                            affectedBlocks -> affectedBlocks.forEach(
                                    block -> tridentEntity.getWorld().strikeLightningEffect(block.getLocation()))
                    );
                    task.cancel();
                }
            }, 0L, 1L);
        }
    }
}
