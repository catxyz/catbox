package me.cat.toybox.items;

import me.cat.toybox.impl.abstraction.item.ToyboxItem;
import me.cat.toybox.impl.abstraction.item.ToyboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class TestItem extends ToyboxItem {

    public TestItem() {
        super(
                new ToyboxItemBuilder()
                        .useActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK,
                                Action.LEFT_CLICK_AIR,
                                Action.LEFT_CLICK_BLOCK
                        ))
                        .itemId("test_item")
                        .material(Material.PUFFERFISH)
                        .displayName(Component.text("Test Item", NamedTextColor.RED))
                        .lore(List.of(
                                Component.empty(),
                                Component.text("A secret item.", NamedTextColor.GRAY)
                        ))
                        .cancelUseInteraction(true)
        );
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        player.sendMessage(Component.text("hungry? fed!", NamedTextColor.YELLOW));
        player.setFoodLevel(100);
    }
}
