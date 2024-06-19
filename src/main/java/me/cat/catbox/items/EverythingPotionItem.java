package me.cat.catbox.items;

import me.cat.catbox.impl.abstraction.item.CatboxItem;
import me.cat.catbox.impl.abstraction.item.CatboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public class EverythingPotionItem extends CatboxItem {

    public EverythingPotionItem() {
        super(
                new CatboxItemBuilder()
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
                                ItemFlag.HIDE_ADDITIONAL_TOOLTIP
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
        return Registry.EFFECT.stream()
                .map(potionEffectType -> new PotionEffect(
                        potionEffectType,
                        10 * 20,
                        Integer.MAX_VALUE,
                        true, true, true
                ))
                .toList();
    }
}
