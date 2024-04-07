package me.cat.toybox.items.explosivetoybow;

import com.google.common.base.Preconditions;
import me.cat.toybox.ToyboxPlugin;
import me.cat.toybox.impl.abstraction.item.ToyboxItem;
import me.cat.toybox.impl.abstraction.item.ToyboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ExplosiveToyBowItem extends ToyboxItem implements Listener {

    protected static final NamespacedKey EXPLOSIVE_ARROW_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "explosive_arrow",
            ToyboxPlugin.get()
    ));
    protected static final int DESPAWN_SECONDS = 4;

    public ExplosiveToyBowItem() {
        super(
                new ToyboxItemBuilder()
                        .useActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .itemId("explosive_toy_bow")
                        .material(Material.BOW)
                        .insertData(EXPLOSIVE_ARROW_TAG, PersistentDataType.BOOLEAN, true)
                        .displayName(Component.text("Explosive Toy Bow", NamedTextColor.RED))
                        .lore(List.of(
                                Component.empty(),
                                Component.text("Wouldn't it be unfortunate if this bow", NamedTextColor.GRAY),
                                Component.text("were to spontaneously combust?", NamedTextColor.GRAY)
                        ))
        );
    }

    @Override
    public void loadAdditionalItemData() {
        hookSelfListener(new ExplosiveToyBowListener());
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
    }
}
