package me.cat.toybox.impl.abstraction.sharedlisteners;

import me.cat.toybox.impl.abstraction.item.SharedItemTags;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;

public class FireworkDamageListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework firework) {
            PersistentDataContainer fireworkPdc = firework.getPersistentDataContainer();

            if (fireworkPdc.has(SharedItemTags.USES_FIREWORKS_TAG)) {
                event.setCancelled(true);
            }
        }
    }
}
