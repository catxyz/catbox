package me.cat.toybox.impl.items;

import com.google.common.base.Preconditions;
import me.cat.toybox.ToyboxPlugin;
import me.cat.toybox.impl.abstraction.AbstractItem;
import me.cat.toybox.helpers.Helper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class ExplosiveToyBowItem extends AbstractItem implements Listener {

    private static final NamespacedKey EXPLOSIVE_TOY_BOW_ARROW_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "explosive_toy_bow_arrow",
            ToyboxPlugin.getInstance()
    ));
    private static final long DESPAWN_SECONDS = 4L;

    public ExplosiveToyBowItem() {
        super(
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .setItemId("explosive_toy_bow")
                        .setMaterial(Material.BOW)
                        .addData(EXPLOSIVE_TOY_BOW_ARROW_TAG, PersistentDataType.BOOLEAN, true)
                        .setDisplayName(Component.text("Explosive Toy Bow", NamedTextColor.RED))
                        .setLore(List.of(
                                Component.empty(),
                                Component.text("Wouldn't it be unfortunate if this bow", NamedTextColor.GRAY),
                                Component.text("were to spontaneously combust?", NamedTextColor.GRAY)
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
                if (bow.getItemMeta().getPersistentDataContainer().has(EXPLOSIVE_TOY_BOW_ARROW_TAG)) {
                    AbstractArrow arrow = (AbstractArrow) event.getProjectile();

                    BlockDisplay blockDisplay = player.getWorld().spawn(player.getEyeLocation(), BlockDisplay.class);
                    blockDisplay.setBlock(Bukkit.createBlockData(Material.TNT));
                    blockDisplay.setBillboard(Display.Billboard.CENTER);

                    float explosionPower = ThreadLocalRandom.current().nextFloat(2f, 2.5f);

                    AtomicInteger ticksPassed = new AtomicInteger();
                    AtomicInteger arrowSecondsAlive = new AtomicInteger();
                    Bukkit.getServer().getScheduler().runTaskTimer(ToyboxPlugin.getInstance(), (task) -> {
                        ticksPassed.getAndIncrement();
                        if (ticksPassed.get() % 20 == 0) {
                            arrowSecondsAlive.getAndIncrement();
                        }

                        if (!arrow.isValid()) {
                            Helper.removeEntitiesInStyle(Particle.SONIC_BOOM, 1, blockDisplay);
                            task.cancel();
                        }

                        if (arrowSecondsAlive.get() >= DESPAWN_SECONDS) {
                            Helper.removeEntitiesInStyle(Particle.SONIC_BOOM, 1,
                                    arrow, blockDisplay);
                            task.cancel();
                        }

                        arrow.addPassenger(blockDisplay);

                        if (arrow.isOnGround()) {
                            arrow.getWorld().createExplosion(arrow.getLocation(), explosionPower, true, true);
                            arrow.remove();
                            blockDisplay.remove();
                        }
                    }, 0L, 1L);
                }
            }
        }
    }
}
