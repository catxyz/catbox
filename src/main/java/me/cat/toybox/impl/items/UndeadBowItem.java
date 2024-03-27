package me.cat.toybox.impl.items;

import com.google.common.base.Preconditions;
import me.cat.toybox.ToyboxPlugin;
import me.cat.toybox.helpers.Helper;
import me.cat.toybox.impl.abstraction.AbstractItem;
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

public class UndeadBowItem extends AbstractItem implements Listener {

    private static final NamespacedKey UNDEAD_BOW_ARROW_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "undead_bow_arrow",
            ToyboxPlugin.getInstance()
    ));
    private static final long DESPAWN_SECONDS = 4L;

    public UndeadBowItem() {
        super(
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .setItemId("undead_bow")
                        .setUseCooldown(Duration.ZERO)
                        .setMaterial(Material.BOW)
                        .addData(UNDEAD_BOW_ARROW_TAG, PersistentDataType.BOOLEAN, true)
                        .setDisplayName(Component.text("Undead Bow", NamedTextColor.BLUE))
                        .setLore(List.of(
                                Component.empty(),
                                Component.text("Dead inside!", NamedTextColor.GRAY),
                                Component.text("... or is it?", NamedTextColor.GRAY)
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
                if (bow.getItemMeta().getPersistentDataContainer().has(UNDEAD_BOW_ARROW_TAG)) {
                    event.getProjectile().remove();

                    player.playSound(player.getEyeLocation(), Sound.ENTITY_CAT_PURREOW, 1f, 1f);
                    player.getWorld().spawn(player.getEyeLocation(), WitherSkull.class, witherSkull -> {
                        AtomicInteger witherSkullSecondsAlive = new AtomicInteger();

                        Bukkit.getServer().getScheduler().runTaskTimer(ToyboxPlugin.getInstance(), (task) -> {
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
