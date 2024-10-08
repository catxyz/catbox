package me.cat.catbox.items.beta.portal;

import me.cat.catbox.helpers.NamespaceHelper;
import me.cat.catbox.impl.abstraction.item.CatboxItem;
import me.cat.catbox.impl.abstraction.item.CatboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class PortalItem extends CatboxItem {

    protected static final NamespacedKey IS_PORTAL_DEVICE_TAG = NamespaceHelper.newSelfPluginTag("is_portal_device");
    protected static final double DISTANCE_BETWEEN = 3.0;

    public PortalItem() {
        super(
                new CatboxItemBuilder()
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
                        .markedAsBeta(true)
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
