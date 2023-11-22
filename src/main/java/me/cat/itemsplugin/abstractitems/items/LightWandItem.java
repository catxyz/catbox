package me.cat.itemsplugin.abstractitems.items;

import me.cat.itemsplugin.Helper;
import me.cat.itemsplugin.ItemsPlugin;
import me.cat.itemsplugin.abstractitems.abstraction.AbstractItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class LightWandItem extends AbstractItem {

    private static final long DESPAWN_SECONDS = 3L;
    private Player shooter;

    public LightWandItem() {
        super(
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK,
                                Action.LEFT_CLICK_AIR,
                                Action.LEFT_CLICK_BLOCK
                        ))
                        .setItemId("light_wand")
                        .setUseCooldown(Duration.ofMillis(1_500L))
                        .setMaterial(Material.BLAZE_ROD)
                        .setDisplayName(Component.text("Light Wand", NamedTextColor.GOLD))
                        .setLore(List.of(
                                Component.text("All the colors of light!", NamedTextColor.LIGHT_PURPLE)
                        ))
        );
    }

    @Override
    public void useItemInteraction(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        this.shooter = player;

        player.sendMessage(Component.text("pew!", NamedTextColor.GRAY));
        player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 10f, 1f);

        List<Material> headColors = Arrays.stream(Material.values())
                .filter(material -> material.name().endsWith("_CONCRETE"))
                .toList();
        player.getWorld().spawn(player.getEyeLocation().clone().subtract(0.0d, 0.2d, 0.0d), ArmorStand.class, armorStand -> {
            armorStand.setSmall(true);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.getEquipment()
                    .setHelmet(new ItemStack(headColors.get(ThreadLocalRandom.current().nextInt(headColors.size()))));

            Bukkit.getServer().getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (task) -> {
                if (!armorStand.isValid()) {
                    task.cancel();
                }

                Location armorStandLocation = armorStand.getLocation().clone();
                Vector vec = armorStandLocation.getDirection();
                armorStand.setHeadPose(armorStand.getHeadPose().add(0.0d, 1.0d, 0.0d));
                armorStand.teleport(armorStandLocation.add(vec));
            }, 0L, 0L);
            spawnFireworkOnGroundHit(armorStand);

            runKillEntitiesAroundArmorStandTask(armorStand);

            AtomicInteger armorStandSecondsAlive = new AtomicInteger();
            Bukkit.getServer().getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (task) -> {
                if (!armorStand.isValid()) {
                    task.cancel();
                }
                armorStandSecondsAlive.getAndIncrement();

                if (armorStandSecondsAlive.get() >= DESPAWN_SECONDS) {
                    spawnCustomFirework(armorStand.getWorld(), armorStand.getLocation());
                    armorStand.remove();
                    task.cancel();
                }
            }, 0L, 20L);
        });
    }

    private void spawnFireworkOnGroundHit(ArmorStand armorStand) {
        Bukkit.getServer().getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (task) -> {
            Location armorStandLocation = armorStand.getLocation();

            if (Helper.isOnGround(armorStandLocation)) {
                Location missLoc = armorStandLocation.clone().add(0.0d, 0.9d, 0.0d);
                spawnCustomFirework(armorStand.getWorld(), missLoc);
                armorStand.remove();
                task.cancel();
            }
        }, 0L, 2L);
    }

    private void runKillEntitiesAroundArmorStandTask(ArmorStand armorStand) {
        Bukkit.getServer().getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (task) -> {
            if (!armorStand.isValid()) {
                task.cancel();
            }

            armorStand.getNearbyEntities(2.5d, 2.5d, 2.5d).stream()
                    .filter(entity -> entity instanceof LivingEntity)
                    .filter(entity -> !(entity instanceof ArmorStand))
                    .forEach(entity -> damageEntity(entity, entity.isValid()));
        }, 0L, 10L);
    }

    private void damageEntity(Entity entity, boolean entityAlive) {
        if (entityAlive) {
            double damageToDeal = ThreadLocalRandom.current().nextDouble(999.0d, 1_999.0d);
            LivingEntity livingEntity = (LivingEntity) entity;
            if (entity.getUniqueId() != shooter.getUniqueId()) {
                livingEntity.setAI(false);
                livingEntity.addPotionEffect(new PotionEffect(
                        PotionEffectType.LEVITATION,
                        2 * 20,
                        1,
                        true,
                        false,
                        false
                ));
                livingEntity.damage(damageToDeal);
                shooter.sendMessage(Component.text("You dealt ", NamedTextColor.GRAY)
                        .append(Component.text(Helper.formatNum(damageToDeal), NamedTextColor.RED))
                        .append(Component.text(" damage to ", NamedTextColor.GRAY))
                        .append(Component.text(livingEntity.getName(), NamedTextColor.YELLOW))
                        .append(Component.text('!', NamedTextColor.GRAY)));
                spawnCustomFirework(entity.getWorld(), entity.getLocation());
            }
        }
    }

    private void spawnCustomFirework(World world, Location victimEntityLoc) {
        int rgbLimit = 255;

        FireworkEffect fireworkEffect = FireworkEffect.builder()
                .withColor(Color.fromRGB(
                        ThreadLocalRandom.current().nextInt(rgbLimit),
                        ThreadLocalRandom.current().nextInt(rgbLimit),
                        ThreadLocalRandom.current().nextInt(rgbLimit)
                ))
                .with(FireworkEffect.Type.BALL)
                .trail(true)
                .flicker(true)
                .build();

        Firework firework = world.spawn(victimEntityLoc, Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(fireworkEffect);
        fireworkMeta.setPower(0);
        firework.setFireworkMeta(fireworkMeta);
        firework.setTicksToDetonate(0);

        firework.getNearbyEntities(2.5d, 2.5d, 2.5d).stream()
                .filter(entity -> entity instanceof Player && entity.getUniqueId() != shooter.getUniqueId())
                .forEach(playerEntity -> {
                    double x = playerEntity.getLocation().getDirection().getX();
                    double z = playerEntity.getLocation().getDirection().getZ();

                    playerEntity.setVelocity(new Vector(-x, 0.7d, -z));
                });
    }
}
