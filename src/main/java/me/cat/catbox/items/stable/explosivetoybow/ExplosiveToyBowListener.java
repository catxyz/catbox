package me.cat.catbox.items.stable.explosivetoybow;

import me.cat.catbox.helpers.LieDetectionHelper;
import me.cat.catbox.helpers.LoopHelper;
import me.cat.catbox.helpers.MiscHelper;
import me.cat.catbox.impl.abstraction.interfaces.EntityLifetimeLooper;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Collections;
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
        if (entities.length == 0 || LieDetectionHelper.arrayHasNull(entities)) {
            return;
        }
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
                MiscHelper.removeEntitiesInStyle(Particle.GUST_EMITTER_LARGE, 1, tntBlockDisplay);
                task.cancel();
            }

            if (arrowSecondsAlive.get() >= ExplosiveToyBowItem.DESPAWN_SECONDS) {
                MiscHelper.removeEntitiesInStyle(Particle.GUST_EMITTER_LARGE, 1, arrow, tntBlockDisplay);
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
                World world = arrow.getWorld();
                Location arrowLoc = arrow.getLocation();

                MiscHelper.createSurfaceLayer(world, arrowLoc, 3,
                        Collections.emptyList(),
                        true,
                        affectedBlocks -> affectedBlocks.forEach(block -> {
                            Location modBlockLoc = block.getLocation()
                                    .clone()
                                    .add(new Vector(0, 1.5, 0));
                            world.spawnParticle(Particle.GUST_EMITTER_SMALL, modBlockLoc, 1);
                        }));
                world.createExplosion(arrowLoc, getExplosionPower(), false, false);
                arrow.remove();
            }
        }
    }

    private float getExplosionPower() {
        return ThreadLocalRandom.current().nextFloat(1.5f, 3.0f);
    }
}
