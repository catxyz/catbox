package me.cat.catbox.items.explosivetoybow;

import me.cat.catbox.helpers.Helper;
import me.cat.catbox.helpers.LoopHelper;
import me.cat.catbox.impl.abstraction.interfaces.EntityLifetimeLooper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class ExplosiveToyBowListener implements Listener, EntityLifetimeLooper {

    private void wrapArrowData(AbstractArrow arrow) {
        arrow.getPersistentDataContainer()
                .set(ExplosiveToyBowItem.EXPLOSIVE_ARROW_TAG, PersistentDataType.BOOLEAN, true);

        BlockDisplay blockDisplay = arrow.getWorld().spawn(arrow.getLocation(), BlockDisplay.class);
        blockDisplay.setBlock(Bukkit.createBlockData(Material.TNT));
        blockDisplay.setBillboard(Display.Billboard.CENTER);

        arrow.addPassenger(blockDisplay);
    }

    @Override
    public void defineLifetimeFor(Entity... entities) {
        AbstractArrow arrow = (AbstractArrow) entities[0];
        BlockDisplay tntBlockDisplay = (BlockDisplay) arrow.getPassengers().getFirst();

        AtomicInteger ticksPassed = new AtomicInteger();
        AtomicInteger arrowSecondsAlive = new AtomicInteger();

        LoopHelper.runIndefinitely(0L, 1L, (task) -> {
            ticksPassed.getAndIncrement();
            if (ticksPassed.get() % 20 == 0) {
                arrowSecondsAlive.getAndIncrement();
            }

            if (!arrow.isValid()) {
                Helper.removeEntitiesInStyle(Particle.SONIC_BOOM, 1, tntBlockDisplay);
                task.cancel();
            }

            if (arrowSecondsAlive.get() >= ExplosiveToyBowItem.DESPAWN_SECONDS) {
                Helper.removeEntitiesInStyle(Particle.SONIC_BOOM, 1, arrow, tntBlockDisplay);
                task.cancel();
            }
        });
    }

    @EventHandler
    public void onProjectileShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            ItemStack bow = event.getBow();
            if (bow != null) {
                PersistentDataContainer bowPdc = bow.getItemMeta().getPersistentDataContainer();

                if (bowPdc.has(ExplosiveToyBowItem.EXPLOSIVE_ARROW_TAG)) {
                    AbstractArrow arrow = (AbstractArrow) event.getProjectile();

                    wrapArrowData(arrow);
                    defineLifetimeFor(arrow);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof AbstractArrow arrow) {
            PersistentDataContainer arrowPdc = arrow.getPersistentDataContainer();

            if (arrowPdc.has(ExplosiveToyBowItem.EXPLOSIVE_ARROW_TAG)) {
                arrow.getWorld().createExplosion(arrow.getLocation(), getExplosionPower(), false, false);
                arrow.remove();
            }
        }
    }

    private float getExplosionPower() {
        return ThreadLocalRandom.current().nextFloat(2f, 2.5f);
    }
}
