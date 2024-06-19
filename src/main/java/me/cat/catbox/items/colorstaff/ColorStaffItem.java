package me.cat.catbox.items.colorstaff;

import me.cat.catbox.helpers.Helper;
import me.cat.catbox.impl.abstraction.item.CatboxItem;
import me.cat.catbox.impl.abstraction.item.CatboxItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.time.Duration;
import java.util.List;

public class ColorStaffItem extends CatboxItem {

    private final ColorStaffListener colorStaffListener;

    public ColorStaffItem() {
        super(
                new CatboxItemBuilder()
                        .useActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK,
                                Action.LEFT_CLICK_AIR,
                                Action.LEFT_CLICK_BLOCK
                        ))
                        .itemId("color_staff")
                        .useCooldown(Duration.ofMillis(500L))
                        .material(Material.BLAZE_ROD)
                        .displayName(Helper.makeComponentColorful(Component.text("Color Staff")))
                        .lore(List.of(
                                Component.empty(),
                                Helper.makeComponentColorful(Component.text("All the colors of light!"))
                        ))
        );

        this.colorStaffListener = new ColorStaffListener();
    }

    @Override
    public void loadAdditionalItemData() {
        hookSelfListener(colorStaffListener);
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        colorStaffListener.onCustomUse(event);
    }
}
