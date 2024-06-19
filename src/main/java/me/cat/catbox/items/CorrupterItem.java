package me.cat.catbox.items;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.google.common.base.Preconditions;
import me.cat.catbox.CatboxPlugin;
import me.cat.catbox.helpers.Helper;
import me.cat.catbox.impl.abstraction.item.CatboxItem;
import me.cat.catbox.impl.abstraction.item.CatboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class CorrupterItem extends CatboxItem implements Listener {

    private static final List<Material> CORRUPTED_MATERIALS;
    private static final NamespacedKey CORRUPTER_TRIDENT_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "corrupter_trident",
            CatboxPlugin.get()
    ));
    private static final int DESPAWN_SECONDS = 6;
    private static final long DELAY_BETWEEN_EFFECT_SECONDS = 6L; // todo -> unused??
    private static final int MIN_RADIUS = 1;
    private static final int MAX_RADIUS = 6;

    // todo -> make these PDC tags instead
    private static int currentCorruptionRadius = 3;
    private static boolean trailingEffectToggled = false;

    static {
        CORRUPTED_MATERIALS = Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .filter(material -> MaterialSetTag.WOOL.isTagged(material) || MaterialTags.CONCRETES.isTagged(material))
                .toList();
    }

    public CorrupterItem() {
        super(
                new CatboxItemBuilder()
                        .useActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK,
                                Action.LEFT_CLICK_AIR,
                                Action.LEFT_CLICK_BLOCK
                        ))
                        .itemId("corrupter")
                        .useCooldown(Duration.ZERO)
                        .material(Material.TRIDENT)
                        .enchants(Map.of(
                                Enchantment.LOYALTY, 5
                        ))
                        .insertData(CORRUPTER_TRIDENT_TAG, PersistentDataType.BOOLEAN, true)
                        .itemFlags(List.of(
                                ItemFlag.HIDE_ENCHANTS,
                                ItemFlag.HIDE_ATTRIBUTES
                        ))
                        .displayName(Component.text("Corrupter", NamedTextColor.DARK_PURPLE))
                        .lore(generateDynamicItemLore(
                                currentCorruptionRadius,
                                trailingEffectToggled
                        ))
        );
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            PlayerInventory playerInventory = player.getInventory();
            if (player.isSneaking()) {
                if (player.getEyeLocation().getPitch() == -90.0f) {
                    trailingEffectToggled = !trailingEffectToggled;
                    player.sendMessage(getTrailingEffectToggleComponent(trailingEffectToggled));
                } else {
                    if (currentCorruptionRadius <= MIN_RADIUS) {
                        player.sendMessage(getInvalidRadiusComponent());
                        return;
                    }
                    currentCorruptionRadius--;
                    player.sendMessage(getRadiusUpdateComponent(currentCorruptionRadius));
                }
            } else {
                if (currentCorruptionRadius >= MAX_RADIUS) {
                    player.sendMessage(getInvalidRadiusComponent());
                    return;
                }
                currentCorruptionRadius++;
                player.sendMessage(getRadiusUpdateComponent(currentCorruptionRadius));
            }

            ItemStack updatedItem = builder()
                    .lore(generateDynamicItemLore(
                            currentCorruptionRadius,
                            trailingEffectToggled
                    ))
                    .toItemStack();
            playerInventory.setItemInMainHand(updatedItem);
        }
    }

    @EventHandler
    public void onPlayerLaunchProjectile(PlayerLaunchProjectileEvent event) {
        Player player = event.getPlayer();

        ItemStack trident = event.getItemStack();
        if (trident.getItemMeta().getPersistentDataContainer().has(CORRUPTER_TRIDENT_TAG)) {
            Trident tridentEntity = (Trident) event.getProjectile();

            Bukkit.getServer()
                    .getScheduler()
                    .runTaskTimer(CatboxPlugin.get(),
                            (muteLightningTask) -> {
                                if (!tridentEntity.isValid()) {
                                    muteLightningTask.cancel();
                                }
                                player.stopSound(Sound.ENTITY_LIGHTNING_BOLT_IMPACT);
                                player.stopSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
                            }, 0L, 1L);

            AtomicInteger ticksPassed = new AtomicInteger();
            AtomicInteger tridentSecondsAlive = new AtomicInteger();

            Bukkit.getServer().getScheduler().runTaskTimer(CatboxPlugin.get(), (task) -> {
                ticksPassed.getAndIncrement();
                if (ticksPassed.get() % 20 == 0) {
                    tridentSecondsAlive.getAndIncrement();
                }

                if (!tridentEntity.isValid()) {
                    returnTridentToPlayerIfNecessary(tridentEntity, player, null);
                    task.cancel();
                }

                if (tridentSecondsAlive.get() >= DESPAWN_SECONDS) {
                    returnTridentToPlayerIfNecessary(tridentEntity, player, () -> {
                        Helper.removeEntitiesInStyle(Particle.SONIC_BOOM, 1, tridentEntity);
                        task.cancel();
                    });
                }

                if (!player.isValid()) {
                    Helper.removeEntitiesInStyle(Particle.SONIC_BOOM, 1, tridentEntity);
                    task.cancel();
                }

                playCorruptionEffect(player, tridentEntity, task);
            }, 0L, 1L);
        }
    }

    private void playCorruptionEffect(Player player, Trident tridentEntity, BukkitTask hookedTask) {
        // saved in a variable so it doesn't recalculate
        boolean shouldPlayEffect = tridentEntity.isOnGround();

        if (shouldPlayEffect) {
            World tridentWorld = tridentEntity.getWorld();

            List<Material> flowers = Stream.concat(
                    MaterialSetTag.FLOWERS.getValues().stream()
                            .filter(material -> !material.isCollidable())
                            .filter(material -> material != Material.SPORE_BLOSSOM),
                    MaterialSetTag.TALL_FLOWERS.getValues().stream()
            ).toList();

            Helper.createSurfaceLayer(
                    tridentWorld,
                    tridentEntity.getLocation(),
                    currentCorruptionRadius,
                    CORRUPTED_MATERIALS,
                    true,
                    affectedBlocks -> affectedBlocks.forEach(block -> {
                        Location locAboveAffectedBlock = block.getLocation()
                                .add(0.5d, 1.0d, 0.5d);
                        Block blockAbove = locAboveAffectedBlock.getBlock();

                        if (Helper.chanceOfHappening(50)) {
                            Material pickedFlower = Helper.randListElem(flowers);

                            if (blockAbove.getType() == Material.AIR && !blockAbove.isLiquid()) {
                                for (Player observer : Bukkit.getServer().getOnlinePlayers()) {
                                    observer.sendBlockChange(locAboveAffectedBlock, pickedFlower.createBlockData());
                                }
                            }
                        }

                        if (Helper.chanceOfHappening(4)) {
                            tridentWorld.spawn(locAboveAffectedBlock, EvokerFangs.class, evokerFangs -> {
                                evokerFangs.setOwner(player);
                                tridentWorld.spawnParticle(Particle.SONIC_BOOM, evokerFangs.getLocation(), 1);
                            });
                        }
                    })
            );

            boolean shouldStopPlayingEffect = !trailingEffectToggled;
            if (shouldStopPlayingEffect) {
                hookedTask.cancel();
            }
        }
    }

    private void returnTridentToPlayerIfNecessary(Trident tridentEntity, Player player, Runnable runAfterReturning) {
        if (!tridentEntity.isOnGround() || !tridentEntity.isValid()) {
            GameMode playerGameMode = player.getGameMode();
            if (playerGameMode == GameMode.SURVIVAL || playerGameMode == GameMode.ADVENTURE) {
                ItemStack newTrident = getSelfItemStack();

                PlayerInventory playerInventory = player.getInventory();
                if (!playerInventory.contains(newTrident)) {
                    playerInventory.addItem(newTrident);
                }
            }
            if (runAfterReturning != null) {
                runAfterReturning.run();
            }
        }
    }

    private static List<Component> generateDynamicItemLore(int providedCorruptionRadius,
                                                           boolean trailingEffectToggled) {
        return List.of(
                Component.empty(),
                Component.text("A totally safe tool!", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Radius: ", NamedTextColor.YELLOW)
                        .append(Component.text(providedCorruptionRadius, NamedTextColor.WHITE)),
                Component.text("Trailing effect: ", NamedTextColor.YELLOW)
                        .append(Helper.enabledOrDisabled(trailingEffectToggled)),
                Component.empty(),
                Component.text("(Left-click) to increase radius!", NamedTextColor.GREEN),
                Component.text("(Sneak + Left-click) to decrease radius!", NamedTextColor.RED),
                Component.text("(Sneak + Left-click + look directly up)", NamedTextColor.LIGHT_PURPLE),
                Component.text(" to toggle trailing effect!", NamedTextColor.LIGHT_PURPLE)
        );
    }

    private TextComponent getRadiusUpdateComponent(int providedCorruptionRadius) {
        return Component.newline()
                .append(Component.text("Radius updated to ", NamedTextColor.GREEN)
                        .append(Component.text(providedCorruptionRadius, NamedTextColor.YELLOW))
                        .appendNewline());
    }

    private TextComponent getTrailingEffectToggleComponent(boolean trailingEffectToggled) {
        return Component.newline()
                .append(Component.text("Trailing effect ", NamedTextColor.YELLOW)
                        .append(Helper.enabledOrDisabled(trailingEffectToggled))
                        .appendNewline());
    }

    private TextComponent getInvalidRadiusComponent() {
        return Component.newline()
                .append(Component.text("Radius can't be ", NamedTextColor.RED)
                        .append(Component.text("<" + MIN_RADIUS + " or >" + MAX_RADIUS, NamedTextColor.YELLOW))
                        .append(Component.text('!', NamedTextColor.RED)
                                .appendNewline()));
    }
}
