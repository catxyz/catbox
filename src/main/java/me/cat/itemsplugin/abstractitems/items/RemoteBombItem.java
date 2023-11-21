package me.cat.itemsplugin.abstractitems.items;

import com.google.common.base.Preconditions;
import me.cat.itemsplugin.Helper;
import me.cat.itemsplugin.ItemsPlugin;
import me.cat.itemsplugin.abstractitems.abstraction.AbstractItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
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

public class RemoteBombItem extends AbstractItem implements Listener {

    private static final NamespacedKey REMOTE_BOMB_ARROW_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "remote_bomb_arrow",
            ItemsPlugin.getInstance()
    ));
    private static final long DESPAWN_SECONDS = 4L;

    public RemoteBombItem() {
        super(
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .setItemId("remote_bomb")
                        .setMaterial(Material.BOW)
                        .addData(REMOTE_BOMB_ARROW_TAG, PersistentDataType.BOOLEAN, true)
                        .setDisplayName(Component.text("Remote Bomb", NamedTextColor.DARK_RED))
                        .setLore(List.of(
                                Component.text("Wouldn't it be unfortunate", NamedTextColor.GRAY),
                                Component.text("if this were to spontaneously combust?", NamedTextColor.GRAY)
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
                if (bow.getItemMeta().getPersistentDataContainer().has(REMOTE_BOMB_ARROW_TAG)) {
                    BlockDisplay blockDisplay = player.getWorld().spawn(player.getEyeLocation(), BlockDisplay.class);
                    blockDisplay.setBlock(Bukkit.createBlockData(Material.TNT));
                    blockDisplay.setBillboard(Display.Billboard.CENTER);

                    float explosionPower = ThreadLocalRandom.current().nextFloat(35f, 50f);

                    AtomicInteger ticksPassed = new AtomicInteger();
                    AtomicInteger arrowSecondsAlive = new AtomicInteger();
                    Bukkit.getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (task) -> {
                        ticksPassed.getAndIncrement();
                        if (ticksPassed.get() % 20 == 0) {
                            arrowSecondsAlive.getAndIncrement();
                        }

                        Arrow arrow = (Arrow) event.getProjectile();
                        if (!arrow.isValid()) {
                            Helper.removeEntitiesInStyle(Particle.SONIC_BOOM, 1, blockDisplay);
                            task.cancel();
                        }

                        if (arrowSecondsAlive.get() >= DESPAWN_SECONDS) {
                            Helper.removeEntitiesInStyle(Particle.SONIC_BOOM, 1, arrow, blockDisplay);
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
