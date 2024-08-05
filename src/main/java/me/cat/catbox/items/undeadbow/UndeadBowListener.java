package me.cat.catbox.items.undeadbow;

import me.cat.catbox.helpers.LoopHelper;
import me.cat.catbox.helpers.MiscHelper;
import me.cat.catbox.impl.abstraction.interfaces.EntityLifetimeLooper;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.concurrent.atomic.AtomicInteger;

public class UndeadBowListener implements Listener, EntityLifetimeLooper {

    @Override
    public void defineLifetimeFor(Entity... entities) {
        if (entities.length == 0) {
            return;
        }
        WitherSkull witherSkull = (WitherSkull) entities[0];

        AtomicInteger witherSkullSecondsAlive = new AtomicInteger();

        LoopHelper.runIndefinitely(0L, 20L, (task) -> {
            witherSkullSecondsAlive.getAndIncrement();

            if (!witherSkull.isValid()) {
                task.cancel();
            }
            if (witherSkullSecondsAlive.get() >= UndeadBowItem.DESPAWN_SECONDS) {
                MiscHelper.removeEntitiesInStyle(Particle.SONIC_BOOM, 1, witherSkull);
                task.cancel();
            }
        });
    }

    @EventHandler
    public void onProjectileShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack bow = event.getBow();
            if (bow != null) {
                PersistentDataContainer bowPdc = bow.getItemMeta().getPersistentDataContainer();

                if (bowPdc.has(UndeadBowItem.UNDEAD_ARROW_TAG)) {
                    event.getProjectile().remove();

                    player.playSound(player.getEyeLocation(), Sound.ENTITY_CAT_PURREOW, 1f, 1f);
                    player.getWorld()
                            .spawn(player.getEyeLocation(), WitherSkull.class, this::defineLifetimeFor);
                }
            }
        }
    }
}
