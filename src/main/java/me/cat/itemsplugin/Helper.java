package me.cat.itemsplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Helper {

    public static Component playActivatedMessage(Component content) {
        return Component.text(" -> ", NamedTextColor.GREEN)
                .append(Component.text("Activated ", NamedTextColor.GREEN))
                .append(content)
                .append(Component.text('!', NamedTextColor.GREEN));
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

    public static void createSurfaceLayer(World world, Location center, int radius, List<Material> corruptedMaterials) {
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));

                if (distance <= radius) {
                    int y = world.getHighestBlockYAt(x, z);
                    Location loc = new Location(world, x, y, z);
                    Block block = loc.getBlock();

                    if (block.getType() != Material.AIR && !block.isLiquid()) {
                        Material corruptedMaterial = corruptedMaterials.get(ThreadLocalRandom.current().nextInt(corruptedMaterials.size()));
                        if (corruptedMaterial.isBlock() && !corruptedMaterial.isLegacy()) {
                            block.setType(corruptedMaterial);
                        }
                    }
                }
            }
        }
    }
}
