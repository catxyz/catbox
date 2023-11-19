package me.cat.itemsplugin.abstractitems.items;

import com.google.common.base.Preconditions;
import me.cat.itemsplugin.ItemsPlugin;
import me.cat.itemsplugin.abstractitems.abstraction.AbstractItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.List;

public class WitherBowItem extends AbstractItem implements Listener {

    private static final NamespacedKey WITHER_BOW_ARROW_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "wither_bow_arrow",
            ItemsPlugin.getInstance()
    ));

    public WitherBowItem() {
        super(
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .setItemId("wither_bow")
                        .setMaterial(Material.BOW)
                        .addData(WITHER_BOW_ARROW_TAG, PersistentDataType.BOOLEAN, true)
                        .setDisplayName(Component.text("Wither Bow", NamedTextColor.BLUE))
                        .setLore(List.of(
                                Component.empty(),
                                Component.text("A cool bow!", NamedTextColor.GRAY)
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

                    player.getWorld().spawn(player.getEyeLocation(), WitherSkull.class, witherSkull -> {
                        Bukkit.getServer().getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (task) -> {
                            if (!witherSkull.isValid()) {
                                task.cancel();
                            }
                            witherSkull.addPassenger(player);

                            Location witherSkullLocation = witherSkull.getLocation().clone();
                            Vector vec = witherSkullLocation.getDirection();
                            witherSkull.teleport(witherSkullLocation.add(vec));
                        }, 0L, 20L);
                    });
                }
            }
        }
    }
}
