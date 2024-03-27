package me.cat.toybox.impl.items;

import me.cat.toybox.impl.abstraction.AbstractItem;
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

public class TestificateSpawnerItem extends AbstractItem {

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
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK,
                                Action.LEFT_CLICK_AIR,
                                Action.LEFT_CLICK_BLOCK
                        ))
                        .setItemId("testificate_spawner")
                        .setMaterial(Material.SPAWNER)
                        .setItemFlags(List.of(
                                ItemFlag.HIDE_ITEM_SPECIFICS
                        ))
                        .setDisplayName(Component.text("Testificate Spawner", NamedTextColor.RED))
                        .setLore(List.of(
                                Component.empty(),
                                Component.text("They usually live a very short life.", NamedTextColor.GRAY)
                        ))
        );
    }

    @Override
    public void useItemInteraction(PlayerInteractEvent event) {
        event.setCancelled(true);

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
                villager.setCustomNameVisible(true);
                villager.customName(Component.text(currentRandomName, currentRandomColor));
            });
        }
    }
}
