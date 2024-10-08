package me.cat.catbox.helpers;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class MiscHelper {

    public static Component getGiveItemMessageComponent(String itemId, String playerName) {
        return Component.text("Gave ", NamedTextColor.GREEN)
                .append(Component.text(itemId.toUpperCase(Locale.ROOT), NamedTextColor.YELLOW))
                .append(Component.text(" to ", NamedTextColor.GREEN))
                .append(Component.text(playerName, NamedTextColor.YELLOW))
                .append(Component.text('!', NamedTextColor.GREEN));
    }

    public static Component getActivatedMessageComponent(Component content) {
        return Component.text("Active -> ", NamedTextColor.GREEN)
                .append(content)
                .append(Component.text('!', NamedTextColor.GREEN));
    }

    public static Component getCooldownMessageComponent(Component displayName, Duration useCooldown) {
        return Component.text("Cooldown -> ", NamedTextColor.RED)
                .append(Component.text("use ", NamedTextColor.RED))
                .append(displayName)
                .append(Component.text(" again in ", NamedTextColor.RED))
                .append(Component.text(MiscHelper.formatDuration(useCooldown), NamedTextColor.YELLOW))
                .append(Component.text('!', NamedTextColor.RED));
    }

    public static Component enabledOrDisabled(boolean expression) {
        if (expression) {
            return Component.text("enabled", NamedTextColor.GREEN);
        }
        return Component.text("disabled", NamedTextColor.RED);
    }

    public static Component makeComponentColorful(TextComponent component) {
        int rgbLimit = 255;

        String originalComponentContent = component.content();
        Component newComponent = Component.empty();

        for (int i = 0; i < originalComponentContent.length(); i++) {
            TextColor textColor = TextColor.color(
                    ThreadLocalRandom.current().nextInt(rgbLimit),
                    ThreadLocalRandom.current().nextInt(rgbLimit),
                    ThreadLocalRandom.current().nextInt(rgbLimit)
            );

            newComponent = newComponent.append(Component.text(originalComponentContent.charAt(i), textColor));
        }
        return newComponent;
    }

    public static boolean chanceOfHappening(int chance) {
        return ThreadLocalRandom.current().nextInt(100) < chance;
    }

    public static int randNumBetween(int least, int most) {
        return ThreadLocalRandom.current().nextInt(least, most + 1);
    }

    public static <T> T randListElem(List<T> list) {
        if (list.size() == 1) {
            return list.getFirst();
        }
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public static String formatNum(Object number) {
        double d = Double.parseDouble(new DecimalFormat("#.#").format(number));
        return NumberFormat.getInstance().format(d);
    }

    public static boolean isLooselyOnGround(Location location) {
        Block blockBelow = location.clone()
                .subtract(0, 0.1, 0)
                .getBlock();
        return blockBelow.getType() != Material.AIR && blockBelow.isSolid();
    }

    public static void removeEntitiesInStyle(Particle particle, int count, Entity... entities) {
        if (entities.length == 0 || LieDetectionHelper.arrayHasNull(entities)) {
            return;
        }

        Arrays.stream(entities)
                .filter(Entity::isValid)
                .map(Entity::getLocation)
                .forEach(location -> location.getWorld().spawnParticle(particle, location, count));
        Arrays.stream(entities)
                .filter(Entity::isValid)
                .forEach(Entity::remove);
    }

    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        long millis = duration.toMillisPart();

        return String.format("%,dh %dm %ds %,dms", hours, minutes, seconds, millis);
    }

    public static void createSurfaceLayer(World world, Location center, int radius,
                                          List<Material> materials,
                                          boolean useFakeBlocks,
                                          Consumer<List<Block>> affectedBlocksConsumer) {
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();

        List<Block> affectedBlocksList = Lists.newArrayList();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                double distance = Math.sqrt(Math.pow(x - centerX, 2.0) + Math.pow(z - centerZ, 2.0));

                if (distance <= radius) {
                    int y = world.getHighestBlockYAt(x, z);
                    Location blockLocation = new Location(world, x, y, z);

                    Block block = blockLocation.getBlock();
                    affectedBlocksList.add(block);

                    if (materials.isEmpty()) {
                        continue;
                    }

                    if (block.getType() != Material.AIR && !block.isLiquid()) {
                        Material randMaterial = materials.get(ThreadLocalRandom.current().nextInt(materials.size()));

                        if (randMaterial.isBlock() && !randMaterial.isLegacy()) {
                            if (useFakeBlocks) {
                                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                                    player.sendBlockChange(block.getLocation(), randMaterial.createBlockData());
                                }
                            } else {
                                block.setType(randMaterial);
                            }
                        }
                    }
                }
            }
        }

        affectedBlocksConsumer.accept(affectedBlocksList);
    }
}
