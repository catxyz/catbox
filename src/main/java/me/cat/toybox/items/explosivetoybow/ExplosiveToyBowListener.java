package me.cat.toybox.items.explosivetoybow;

import me.cat.toybox.helpers.Helper;
import me.cat.toybox.helpers.LoopHelper;
import me.cat.toybox.impl.abstraction.interfaces.EntityLifetimeLooper;
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

    @Override
    public void defineLifetimeFor(Entity... entities) {
        Arrow arrow = (Arrow) entities[0];
        BlockDisplay tntBlockDisplay = (BlockDisplay) arrow.getPassengers().get(0);

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

    private void wrapArrowData(Arrow arrow) {
        arrow.getPersistentDataContainer()
                .set(ExplosiveToyBowItem.EXPLOSIVE_ARROW_TAG, PersistentDataType.BOOLEAN, true);

        BlockDisplay blockDisplay = arrow.getWorld().spawn(arrow.getLocation(), BlockDisplay.class);
        blockDisplay.setBlock(Bukkit.createBlockData(Material.TNT));
        blockDisplay.setBillboard(Display.Billboard.CENTER);

        arrow.addPassenger(blockDisplay);
    }

    @EventHandler
    public void onProjectileShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            ItemStack bow = event.getBow();
            if (bow != null) {
                PersistentDataContainer bowPdc = bow.getItemMeta().getPersistentDataContainer();

                if (bowPdc.has(ExplosiveToyBowItem.EXPLOSIVE_ARROW_TAG)) {
                    Arrow arrow = (Arrow) event.getProjectile();

                    wrapArrowData(arrow);
                    defineLifetimeFor(arrow);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow) {
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
