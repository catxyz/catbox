package me.cat.abstractitems.abstractitems.items;

import me.cat.abstractitems.abstractitems.abstraction.AbstractItem;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class TestItem extends AbstractItem {

    public TestItem() {
        super(
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .setItemId("test_item")
                        .setMaterial(Material.STONE)
        );
    }

    @Override
    public void useItemInteraction(PlayerInteractEvent event) {
        event.getPlayer().sendMessage("test");
    }
}
