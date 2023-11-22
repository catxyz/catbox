package me.cat.itemsplugin.abstractitems.items;

import me.cat.itemsplugin.abstractitems.abstraction.AbstractItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class MyPreciousItem extends AbstractItem {

    public MyPreciousItem() {
        super(
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .setItemId("my_precious")
                        .setMaterial(Material.EMERALD)
                        .setDisplayName(Component.text("My Precious", NamedTextColor.BLUE))
                        .setLore(List.of(
                                Component.text("They stole it from us.", NamedTextColor.GRAY, TextDecoration.ITALIC),
                                Component.text("Master betrayed us.", NamedTextColor.GRAY, TextDecoration.ITALIC)
                        ))
        );
    }

    @Override
    public void useItemInteraction(PlayerInteractEvent event) {
    }
}