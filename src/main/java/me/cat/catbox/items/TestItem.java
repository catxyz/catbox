package me.cat.catbox.items;

import me.cat.catbox.impl.abstraction.item.CatboxItem;
import me.cat.catbox.impl.abstraction.item.CatboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class TestItem extends CatboxItem {

    public TestItem() {
        super(
                new CatboxItemBuilder()
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

        player.sendMessage(Component.text("\uD83D\uDD1A Hunger has left the chat",
                NamedTextColor.GRAY, TextDecoration.ITALIC));
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 10f, 1.1f);
        player.setFoodLevel(20);
        player.setSaturation(20f);
    }
}
