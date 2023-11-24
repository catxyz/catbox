package me.cat.itemsplugin.abstractitems.items;

import com.google.common.base.Preconditions;
import me.cat.itemsplugin.Helper;
import me.cat.itemsplugin.ItemsPlugin;
import me.cat.itemsplugin.abstractitems.abstraction.AbstractItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WitherBowItem extends AbstractItem implements Listener {

    private static final NamespacedKey WITHER_BOW_ARROW_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "wither_bow_arrow",
            ItemsPlugin.getInstance()
    ));
    private static final long DESPAWN_SECONDS = 4L;

    public WitherBowItem() {
        super(
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .setItemId("wither_bow")
                        .setUseCooldown(Duration.ZERO)
                        .setMaterial(Material.BOW)
                        .addData(WITHER_BOW_ARROW_TAG, PersistentDataType.BOOLEAN, true)
                        .setDisplayName(Component.text("Wither Bow", NamedTextColor.BLUE))
                        .setLore(List.of(
                                Component.empty(),
                                Component.text("Dead inside!", NamedTextColor.GRAY)
                        ))
        );
    }

    @Override
    public void useItemInteraction(PlayerInteractEvent event) {
    }

    @EventHandler
    public void onProjectileShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack bow = event.getBow();
            if (bow != null) {
                if (bow.getItemMeta().getPersistentDataContainer().has(WITHER_BOW_ARROW_TAG)) {
                    event.getProjectile().remove();

                    player.playSound(player.getEyeLocation(), Sound.ENTITY_WITHER_SHOOT, 10f, 1.6f);
                    player.getWorld().spawn(player.getEyeLocation(), WitherSkull.class, witherSkull -> {
                        AtomicInteger witherSkullSecondsAlive = new AtomicInteger();

                        Bukkit.getServer().getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (task) -> {
                            witherSkullSecondsAlive.getAndIncrement();

                            if (!witherSkull.isValid()) {
                                task.cancel();
                            }
                            if (witherSkullSecondsAlive.get() >= DESPAWN_SECONDS) {
                                Helper.removeEntitiesInStyle(Particle.SONIC_BOOM, 1, witherSkull);
                                task.cancel();
                            }
                        }, 0L, 20L);
                    });
                }
            }
        }
    }
}
