package me.cat.toybox.items;

import me.cat.toybox.ToyboxPlugin;
import me.cat.toybox.impl.abstraction.item.ToyboxItem;
import me.cat.toybox.impl.abstraction.item.ToyboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class MyPreciousItem extends ToyboxItem {

    private boolean isSoundEffectPlaying = false;
    private final int delayFactor = 5;

    public MyPreciousItem() {
        super(
                new ToyboxItemBuilder()
                        .useActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .itemId("my_precious")
                        .material(Material.EMERALD)
                        .displayName(Component.text("My Precious", NamedTextColor.LIGHT_PURPLE))
                        .lore(List.of(
                                Component.empty(),
                                Component.text("They stole it from us.", NamedTextColor.GRAY, TextDecoration.ITALIC),
                                Component.text("Master betrayed us.", NamedTextColor.GRAY, TextDecoration.ITALIC)
                        ))
        );
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.DARKNESS,
                10 * 20,
                255,
                false, true, false
        ));

        if (!isSoundEffectPlaying) {
            AtomicInteger soundDelay = new AtomicInteger(0);
            new BukkitRunnable() {
                float pitch = 0f;
                final TextComponent subtitle = Component.text("doesn't like you!", NamedTextColor.RED);

                @Override
                public void run() {
                    int randVillagerNameIndex = ThreadLocalRandom.current().nextInt(TestificateSpawnerItem.TESTIFICATE_NAMES.length);
                    String chosenVillager = TestificateSpawnerItem.TESTIFICATE_NAMES[randVillagerNameIndex];

                    if (pitch >= 2f) {
                        player.showTitle(Title.title(
                                Component.text(chosenVillager, NamedTextColor.YELLOW),
                                subtitle,
                                Title.Times.times(
                                        Duration.ZERO,
                                        Duration.ofMillis(2_500L),
                                        Duration.ofMillis(500L)
                                )
                        ));

                        Bukkit.getServer().getScheduler().runTaskLater(ToyboxPlugin.get(), () ->
                                player.showTitle(Title.title(
                                        Component.empty(),
                                        Component.text("goodbye", NamedTextColor.GREEN),
                                        Title.Times.times(
                                                Duration.ZERO,
                                                Duration.ofSeconds(1L),
                                                Duration.ZERO
                                        )
                                )), (1_500L * 20L) / 1_000L);

                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.LEVITATION,
                                10 * 20,
                                127,
                                false, true, false
                        ));
                        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 10f, 0f);

                        Bukkit.getServer().getScheduler().runTaskLater(ToyboxPlugin.get(),
                                () -> player.setHealth(0.0d),
                                (3_000L * 20L) / 1_000L);

                        isSoundEffectPlaying = false;
                        this.cancel();
                    }
                    if (soundDelay.get() % 20 == 0) {
                        player.showTitle(Title.title(
                                Component.text("villager ")
                                        .append(Component.text(chosenVillager, NamedTextColor.YELLOW)),
                                subtitle,
                                Title.Times.times(
                                        Duration.ZERO,
                                        Duration.ofMillis(2_500L),
                                        Duration.ofMillis(500L)
                                )
                        ));

                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 10f, pitch);
                        pitch += 0.1f;
                    }
                    soundDelay.getAndAdd(delayFactor);
                }
            }.runTaskTimer(ToyboxPlugin.get(), 0L, 1L);

            isSoundEffectPlaying = true;
        }
    }
}
