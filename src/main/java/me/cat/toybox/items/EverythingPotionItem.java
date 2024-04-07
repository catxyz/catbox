package me.cat.toybox.items;

import me.cat.toybox.impl.abstraction.item.ToyboxItem;
import me.cat.toybox.impl.abstraction.item.ToyboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class EverythingPotionItem extends ToyboxItem {

    public EverythingPotionItem() {
        super(
                new ToyboxItemBuilder()
                        .useActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK,
                                Action.LEFT_CLICK_AIR,
                                Action.LEFT_CLICK_BLOCK
                        ))
                        .itemId("everything_potion")
                        .material(Material.POTION)
                        .displayName(Component.text("Potion of Everything", NamedTextColor.LIGHT_PURPLE))
                        .lore(List.of(
                                Component.empty(),
                                Component.text("Refreshing!", NamedTextColor.GRAY)
                        ))
                        .itemFlags(List.of(
                                ItemFlag.HIDE_ITEM_SPECIFICS
                        ))
                        .cancelUseInteraction(true)
        );
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        player.showTitle(Title.title(
                Component.empty(),
                Component.text("Refreshing!", NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC)
        ));
        player.addPotionEffects(getAllPotionEffects());
    }

    private List<PotionEffect> getAllPotionEffects() {
        return Arrays.stream(PotionEffectType.values())
                .map(potionEffectType -> new PotionEffect(
                        potionEffectType,
                        10 * 20,
                        Integer.MAX_VALUE,
                        true, true, true
                ))
                .toList();
    }
}
