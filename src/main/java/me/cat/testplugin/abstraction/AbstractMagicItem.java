package me.cat.testplugin.abstraction;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class AbstractMagicItem {

    private MagicItemBuilder builder;

    public static class MagicItemBuilder {

        private String itemId;
        private Material material;

        public String getItemId() {
            return itemId;
        }

        public Material getMaterial() {
            return material;
        }
    }

    public abstract void useItemInteraction(PlayerInteractEvent event);

    public MagicItemBuilder getBuilder() {
        return builder;
    }
}
