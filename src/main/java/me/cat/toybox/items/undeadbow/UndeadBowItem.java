package me.cat.toybox.items.undeadbow;

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

import java.time.Duration;
import java.util.List;

public class UndeadBowItem extends ToyboxItem implements Listener {

    protected static final NamespacedKey UNDEAD_ARROW_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "undead_arrow",
            ToyboxPlugin.get()
    ));
    protected static final long DESPAWN_SECONDS = 4L;

    public UndeadBowItem() {
        super(
                new ToyboxItemBuilder()
                        .useActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .itemId("undead_bow")
                        .useCooldown(Duration.ZERO)
                        .material(Material.BOW)
                        .insertData(UNDEAD_ARROW_TAG, PersistentDataType.BOOLEAN, true)
                        .displayName(Component.text("Undead Bow", NamedTextColor.BLUE))
                        .lore(List.of(
                                Component.empty(),
                                Component.text("Dead inside!", NamedTextColor.GRAY),
                                Component.text("... or is it?", NamedTextColor.GRAY)
                        ))
        );
    }

    @Override
    public void loadAdditionalItemData() {
        hookSelfListener(new UndeadBowListener());
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
    }
}
