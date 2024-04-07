package me.cat.toybox.items.portal;

import com.google.common.base.Preconditions;
import me.cat.toybox.ToyboxPlugin;
import me.cat.toybox.impl.abstraction.item.ToyboxItem;
import me.cat.toybox.impl.abstraction.item.ToyboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class PortalItem extends ToyboxItem {

    protected static final NamespacedKey IS_PORTAL_DEVICE_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "is_portal_device",
            ToyboxPlugin.get()
    ));
    protected static final double DISTANCE_BETWEEN = 3.0d;

    public PortalItem() {
        super(
                new ToyboxItemBuilder()
                        .useActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .itemId("aperture_science_handheld_portal_device")
                        .material(Material.WHITE_CANDLE)
                        .insertData(IS_PORTAL_DEVICE_TAG, PersistentDataType.BOOLEAN, true)
                        .displayName(Component.text("Aperture Science Handheld Portal Device", NamedTextColor.WHITE))
                        .lore(List.of(
                                Component.empty(),
                                Component.text("\uD83C\uDF82", NamedTextColor.YELLOW)
                        ))
                        .cancelUseInteraction(true)
        );
    }

    @Override
    public void loadAdditionalItemData() {
        hookSelfListener(new PortalListener());
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
    }
}
