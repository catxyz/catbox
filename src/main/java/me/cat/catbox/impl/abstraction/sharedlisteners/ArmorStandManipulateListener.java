package me.cat.catbox.impl.abstraction.sharedlisteners;

import me.cat.catbox.impl.abstraction.item.SharedItemTags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.persistence.PersistentDataContainer;

public class ArmorStandManipulateListener implements Listener {

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        PersistentDataContainer armorStandPdc = event.getRightClicked()
                .getPersistentDataContainer();

        if (armorStandPdc.has(SharedItemTags.CUSTOM_ARMOR_STAND_TAG)) {
            event.setCancelled(true);
        }
    }
}
