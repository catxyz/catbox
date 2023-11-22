package me.cat.itemsplugin;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class Helper {

    public static Component getPlayGiveItemMessageComponent(String itemId, String playerName) {
        return Component.text("Gave ", NamedTextColor.GREEN)
                .append(Component.text(itemId.toUpperCase(Locale.ROOT), NamedTextColor.YELLOW))
                .append(Component.text(" to ", NamedTextColor.GREEN))
                .append(Component.text(playerName, NamedTextColor.YELLOW))
                .append(Component.text('!', NamedTextColor.GREEN));
    }

    public static Component getPlayActivatedMessageComponent(Component content) {
        return Component.text("Activated -> ", NamedTextColor.GREEN)
                .append(content)
                .append(Component.text('!', NamedTextColor.GREEN));
    }

    public static Component getPlayCooldownMessageComponent(Component displayName, Duration useCooldown) {
        return Component.text("Cooldown -> ", NamedTextColor.RED)
                .append(Component.text("use ", NamedTextColor.RED))
                .append(displayName)
                .append(Component.text(" again in ", NamedTextColor.RED))
                .append(Component.text(Helper.formatDuration(useCooldown), NamedTextColor.YELLOW))
                .append(Component.text('!', NamedTextColor.RED));
    }

    public static String formatNum(Object number) {
        double d = Double.parseDouble(new DecimalFormat("#.#").format(number));
        return NumberFormat.getInstance().format(d);
    }

    public static boolean isOnGround(Location location) {
        return location.clone().subtract(0.0d, 0.1d, 0.0d).getBlock().getType() != Material.AIR;
    }

    public static void removeEntitiesInStyle(Particle particle, int count, Entity... entities) {
        Arrays.stream(entities)
                .map(Entity::getLocation)
                .forEach(location -> location.getWorld().spawnParticle(particle, location, count));
        Arrays.stream(entities).forEach(Entity::remove);
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
                                          Consumer<List<Block>> affectedBlocks) {
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));

                if (distance <= radius) {
                    int y = world.getHighestBlockYAt(x, z);
                    Location loc = new Location(world, x, y, z);
                    Block block = loc.getBlock();

                    List<Block> affected = Lists.newArrayList();
                    if (block.getType() != Material.AIR && !block.isLiquid()) {
                        Material material = materials.get(ThreadLocalRandom.current().nextInt(materials.size()));
                        if (material.isBlock() && !material.isLegacy()) {
                            //block.setType(material);
                            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                                player.sendBlockChange(block.getLocation(), material.createBlockData());
                            }

                            affected.add(block);

                            affectedBlocks.accept(affected);
                        }
                    }
                }
            }
        }
    }
}