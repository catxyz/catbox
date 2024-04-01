package me.cat.toybox.items;

import com.destroystokyo.paper.MaterialTags;
import me.cat.toybox.ToyboxPlugin;
import me.cat.toybox.helpers.Helper;
import me.cat.toybox.impl.abstraction.item.SharedItemTags;
import me.cat.toybox.impl.abstraction.item.ToyboxItem;
import me.cat.toybox.impl.abstraction.item.ToyboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class ColorStaffItem extends ToyboxItem {

    private static final List<Material> HEAD_COLORS;
    private static final int DESPAWN_SECONDS = 3;
    private Player shooter;

    static {
        HEAD_COLORS = Arrays.stream(Material.values())
                .filter(MaterialTags.STAINED_GLASS::isTagged)
                .toList();
    }

    public ColorStaffItem() {
        super(
                new ToyboxItemBuilder()
                        .useActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK,
                                Action.LEFT_CLICK_AIR,
                                Action.LEFT_CLICK_BLOCK
                        ))
                        .itemId("color_staff")
                        .useCooldown(Duration.ofMillis(500L))
                        .material(Material.BLAZE_ROD)
                        .displayName(Helper.makeComponentColorful(Component.text("Color Staff")))
                        .lore(List.of(
                                Component.empty(),
                                Helper.makeComponentColorful(Component.text("All the colors of light!"))
                        ))
        );
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        shooter = player;

        player.sendMessage(Component.text("Pew!", NamedTextColor.GRAY));
        player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 10f, 1f);

        player.getWorld().spawn(player.getEyeLocation().clone().subtract(0.0d, 0.2d, 0.0d), ArmorStand.class, armorStand -> {
            armorStand.getPersistentDataContainer()
                    .set(SharedItemTags.CUSTOM_ARMOR_STAND_TAG, PersistentDataType.BOOLEAN, true);

            armorStand.setSmall(true);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.getEquipment()
                    .setHelmet(new ItemStack(HEAD_COLORS.get(ThreadLocalRandom.current().nextInt(HEAD_COLORS.size()))));

            Bukkit.getServer().getScheduler().runTaskTimer(ToyboxPlugin.get(), (task) -> {
                if (!armorStand.isValid()) {
                    task.cancel();
                }

                Location armorStandLocation = armorStand.getLocation().clone();
                Vector vec = armorStandLocation.getDirection();
                armorStand.setHeadPose(armorStand.getHeadPose().add(0.0d, 1.0d, 0.0d));
                armorStand.teleport(armorStandLocation.add(vec));
            }, 0L, 1L);
            spawnFireworkOnGroundHit(armorStand);

            runKillEntitiesAroundArmorStandTask(armorStand);

            AtomicInteger armorStandSecondsAlive = new AtomicInteger();
            Bukkit.getServer().getScheduler().runTaskTimer(ToyboxPlugin.get(), (task) -> {
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
        Bukkit.getServer().getScheduler().runTaskTimer(ToyboxPlugin.get(), (task) -> {
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
        Bukkit.getServer().getScheduler().runTaskTimer(ToyboxPlugin.get(), (task) -> {
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
                        true,
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
        firework.getPersistentDataContainer()
                .set(SharedItemTags.USES_FIREWORKS_TAG, PersistentDataType.BOOLEAN, true);

        firework.getNearbyEntities(2.5d, 2.5d, 2.5d).stream()
                .filter(entity -> entity instanceof Player && entity.getUniqueId() != shooter.getUniqueId())
                .forEach(playerEntity -> {
                    double x = playerEntity.getLocation().getDirection().getX();
                    double z = playerEntity.getLocation().getDirection().getZ();

                    playerEntity.setVelocity(new Vector(-x, 0.7d, -z));
                });
    }
}
