package me.cat.catbox.items.amaceing;

import me.cat.catbox.helpers.NamespaceHelper;
import me.cat.catbox.impl.abstraction.item.CatboxItem;
import me.cat.catbox.impl.abstraction.item.CatboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractWindCharge;
import org.bukkit.entity.BreezeWindCharge;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.util.List;

public class AmaceingItem extends CatboxItem {

    private static final NamespacedKey NUMBER_CHARGES_STORED_TAG = NamespaceHelper.newSelfPluginTag("number_stored_charges");
    private static final NamespacedKey RECHARGING_TAG = NamespaceHelper.newSelfPluginTag("recharging");
    private static final int DEFAULT_CHARGES_NUM = 2;
    private static final Component DISPLAY_NAME_COMPONENT = Component.text('A', NamedTextColor.LIGHT_PURPLE)
            .append(Component.text("mace", NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC))
            .append(Component.text("ing", NamedTextColor.LIGHT_PURPLE));

    public AmaceingItem() {
        super(
                new CatboxItemBuilder()
                        .useActions(List.of(
                                Action.LEFT_CLICK_AIR,
                                Action.LEFT_CLICK_BLOCK
                        ))
                        .itemId("amaceing")
                        .useCooldown(Duration.ofMillis(360L))
                        .material(Material.MACE)
                        .insertData(NUMBER_CHARGES_STORED_TAG, PersistentDataType.INTEGER, DEFAULT_CHARGES_NUM)
                        .insertData(RECHARGING_TAG, PersistentDataType.BOOLEAN, false)
                        .displayName(DISPLAY_NAME_COMPONENT)
                        .lore(List.of(
                                Component.empty(),
                                Component.text("Absolutely!", NamedTextColor.DARK_PURPLE, TextDecoration.ITALIC)
                        ))
                        .itemFlags(List.of(
                                ItemFlag.HIDE_ATTRIBUTES
                        ))
                        .cancelUseInteraction(true)
        );
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        player.getWorld()
                .spawn(player.getLocation(), BreezeWindCharge.class, AbstractWindCharge::explode);
    }
}
