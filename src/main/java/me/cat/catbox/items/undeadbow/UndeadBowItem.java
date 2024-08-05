package me.cat.catbox.items.undeadbow;

import me.cat.catbox.helpers.NamespaceHelper;
import me.cat.catbox.impl.abstraction.item.CatboxItem;
import me.cat.catbox.impl.abstraction.item.CatboxItemBuilder;
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

public class UndeadBowItem extends CatboxItem implements Listener {

    protected static final NamespacedKey UNDEAD_ARROW_TAG = NamespaceHelper.newSelfPluginTag("undead_arrow");
    protected static final long DESPAWN_SECONDS = 4L;

    public UndeadBowItem() {
        super(
                new CatboxItemBuilder()
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
                                Component.text("... Or is it?", NamedTextColor.GRAY)
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
