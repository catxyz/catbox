package me.cat.itemsplugin.abstractitems.items;

import com.google.common.base.Preconditions;
import me.cat.itemsplugin.abstractitems.abstraction.AbstractItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestificateSpawnerItem extends AbstractItem {

    private static final String[] TESTIFICATE_NAMES = new String[]{
            "jerry",
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
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .setItemId("testificate_spawner")
                        .setMaterial(Material.SPAWNER)
                        .setItemFlags(List.of(
                                ItemFlag.HIDE_ITEM_SPECIFICS
                        ))
                        .setDisplayName(Component.text("Testificate Spawner", NamedTextColor.RED))
                        .setLore(List.of(
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

            Location playerTargetBlockLoc = Preconditions.checkNotNull(player.getTargetBlockExact(100)).getLocation();
            player.getWorld().spawn(playerTargetBlockLoc, Villager.class, villager -> {
                villager.setCustomNameVisible(true);
                villager.customName(Component.text(currentRandomName, currentRandomColor));
            });
        }
    }
}
