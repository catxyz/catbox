package me.cat.catbox.items;

import me.cat.catbox.impl.abstraction.item.CatboxItem;
import me.cat.catbox.impl.abstraction.item.CatboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestificateSpawnerItem extends CatboxItem {

    public static final String[] TESTIFICATE_NAMES = new String[]{
            "jerry",
            "barbie",
            "ken",
            "tom",
            "fred",
            "jane",
            "olivia",
            "taylor"
    };

    public TestificateSpawnerItem() {
        super(
                new CatboxItemBuilder()
                        .useActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK,
                                Action.LEFT_CLICK_AIR,
                                Action.LEFT_CLICK_BLOCK
                        ))
                        .itemId("testificate_spawner")
                        .material(Material.SPAWNER)
                        .itemFlags(List.of(
                                ItemFlag.HIDE_ADDITIONAL_TOOLTIP
                        ))
                        .displayName(Component.text("Testificate Spawner", NamedTextColor.RED))
                        .lore(List.of(
                                Component.empty(),
                                Component.text("They usually live a very short life.", NamedTextColor.GRAY)
                        ))
                        .cancelUseInteraction(true)
        );
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        int rgbLimit = 255;
        for (int i = 0; i < 100; i++) {
            String currentRandomName = TESTIFICATE_NAMES[ThreadLocalRandom.current().nextInt(TESTIFICATE_NAMES.length)];
            TextColor currentRandomColor = TextColor.color(
                    ThreadLocalRandom.current().nextInt(rgbLimit),
                    ThreadLocalRandom.current().nextInt(rgbLimit),
                    ThreadLocalRandom.current().nextInt(rgbLimit)
            );

            player.sendMessage(Component.text("Spawned ", NamedTextColor.YELLOW)
                    .append(Component.text(currentRandomName, currentRandomColor)));

            Block playerTargetBlock = player.getTargetBlockExact(100);
            if (playerTargetBlock == null) {
                player.sendMessage(Component.text("Invalid location!", NamedTextColor.RED));
                return;
            }

            player.getWorld().spawn(playerTargetBlock.getLocation().clone().add(0, 1, 0), Villager.class, villager -> {
                villager.customName(Component.text(currentRandomName, currentRandomColor));
                villager.setCustomNameVisible(true);
            });
        }
    }
}
