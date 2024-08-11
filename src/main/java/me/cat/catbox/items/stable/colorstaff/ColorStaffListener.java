package me.cat.catbox.items.stable.colorstaff;

import com.destroystokyo.paper.MaterialTags;
import me.cat.catbox.helpers.LieDetectionHelper;
import me.cat.catbox.helpers.LoopHelper;
import me.cat.catbox.helpers.MiscHelper;
import me.cat.catbox.impl.abstraction.interfaces.CustomUseInteraction;
import me.cat.catbox.impl.abstraction.interfaces.EntityLifetimeLooper;
import me.cat.catbox.impl.abstraction.item.SharedItemTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class ColorStaffListener implements Listener, EntityLifetimeLooper, CustomUseInteraction {

    private static final List<Material> HEAD_COLORS;

    static {
        HEAD_COLORS = Arrays.stream(Material.values())
                .filter(MaterialTags.STAINED_GLASS::isTagged)
                .toList();
    }

    @Override
    public void defineLifetimeFor(Entity... entities) {
        if (entities.length == 0 || LieDetectionHelper.arrayHasNull(entities)) {
            return;
        }
        ArmorStand armorStand = (ArmorStand) entities[0];

        AtomicInteger armorStandSecondsAlive = new AtomicInteger();

        LoopHelper.runIndefinitely(0L, 20L, (task) -> {
            armorStandSecondsAlive.getAndIncrement();

            if (!armorStand.isValid()) {
                task.cancel();
            }

            if (armorStandSecondsAlive.get() >= ColorStaffItem.DESPAWN_SECONDS) {
                spawnCustomFirework(armorStand, armorStand.getLocation());
                armorStand.remove();
                task.cancel();
            }
        });
    }

    @Override
    public void onCustomUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        player.sendMessage(Component.text("Pew!", NamedTextColor.YELLOW));
        player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 10f, 1f);
        spawnArmorStand(player);
    }

    private void spawnArmorStand(Player player) {
        player.getWorld().spawn(player.getEyeLocation().clone().subtract(0, 0.2, 0), ArmorStand.class, armorStand -> {
            PersistentDataContainer armorStandPdc = armorStand.getPersistentDataContainer();
            armorStandPdc.set(ColorStaffItem.ID_OF_USER_TAG, PersistentDataType.STRING, player.getUniqueId().toString());
            armorStandPdc.set(SharedItemTags.CUSTOM_ARMOR_STAND_TAG, PersistentDataType.BOOLEAN, true);

            armorStand.setSmall(true);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            var randomHeadColor = new ItemStack(HEAD_COLORS.get(ThreadLocalRandom.current().nextInt(HEAD_COLORS.size())));
            armorStand.getEquipment()
                    .setHelmet(randomHeadColor);

            LoopHelper.runIndefinitely(0L, 1L, (task) -> {
                if (!armorStand.isValid()) {
                    task.cancel();
                }

                Location armorStandLoc = armorStand.getLocation().clone();
                Vector vec = armorStandLoc.getDirection();
                armorStand.setHeadPose(armorStand.getHeadPose().add(0, 1, 0));
                armorStand.teleport(armorStandLoc.add(vec));
            });

            spawnFireworkIfGroundHit(armorStand);
            runKillEntitiesAroundArmorStandTask(armorStand);
            defineLifetimeFor(armorStand);
        });
    }

    private void spawnFireworkIfGroundHit(ArmorStand armorStand) {
        LoopHelper.runIndefinitely(0L, 2L, (task) -> {
            if (!armorStand.isValid()) {
                task.cancel();
            }

            Location armorStandLocation = armorStand.getLocation();

            if (MiscHelper.isLooselyOnGround(armorStandLocation)) {
                Location landingLoc = armorStandLocation.clone().add(0, 0.9, 0);
                spawnCustomFirework(armorStand, landingLoc);
                armorStand.remove();
                task.cancel();
            }
        });
    }

    private void runKillEntitiesAroundArmorStandTask(ArmorStand armorStand) {
        LoopHelper.runIndefinitely(0L, 10L, (task) -> {
            if (!armorStand.isValid()) {
                task.cancel();
            }

            armorStand.getNearbyEntities(2.5, 2.5, 2.5)
                    .stream()
                    .filter(entity -> entity instanceof LivingEntity)
                    .filter(entity -> !(entity instanceof ArmorStand))
                    .forEach(entity -> damageEntity(armorStand, (LivingEntity) entity));
        });
    }

    private void damageEntity(ArmorStand armorStand, LivingEntity livingEntity) {
        if (livingEntity.isValid()) {
            getStaffUser(armorStand).ifPresent(staffUser -> {
                if (livingEntity.getUniqueId() != staffUser.getUniqueId()) {
                    double damageToApply = ThreadLocalRandom.current().nextDouble(200, Short.MAX_VALUE);
                    int randDuration = 2 * ThreadLocalRandom.current().nextInt(15, 20);

                    livingEntity.addPotionEffects(Set.of(
                            new PotionEffect(
                                    PotionEffectType.LEVITATION,
                                    randDuration,
                                    1,
                                    true,
                                    true,
                                    false
                            ),
                            new PotionEffect(
                                    PotionEffectType.GLOWING,
                                    randDuration,
                                    1,
                                    true,
                                    true,
                                    false
                            )
                    ));

                    LoopHelper.runAfter(randDuration, (task) -> {
                        Vector entityLocDir = livingEntity.getLocation().getDirection();

                        double x = entityLocDir.getX();
                        double z = entityLocDir.getZ();

                        livingEntity.setVelocity(new Vector(-x, 0.7, -z));
                    });

                    LoopHelper.runAfter(randDuration + 10L, (task) -> {
                        staffUser.sendMessage(Component.text("You dealt ", NamedTextColor.GRAY)
                                .append(Component.text(MiscHelper.formatNum(damageToApply), NamedTextColor.RED))
                                .append(Component.text(" damage to ", NamedTextColor.GRAY))
                                .append(Component.text(livingEntity.getName(), NamedTextColor.YELLOW))
                                .append(Component.text('!', NamedTextColor.GRAY)));

                        livingEntity.damage(damageToApply);
                        spawnCustomFirework(armorStand, livingEntity.getLocation());
                    });
                }
            });
        }
    }

    private void spawnCustomFirework(ArmorStand armorStand, Location victimEntityLoc) {
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

        armorStand.getWorld().spawn(victimEntityLoc, Firework.class, firework -> {
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            fireworkMeta.addEffect(fireworkEffect);
            fireworkMeta.setPower(0);
            firework.setFireworkMeta(fireworkMeta);
            firework.setTicksToDetonate(0);
            firework.getPersistentDataContainer()
                    .set(SharedItemTags.USES_FIREWORKS_TAG, PersistentDataType.BOOLEAN, true);

            firework.getNearbyEntities(2.5, 2.5, 2.5)
                    .stream()
                    .filter(entity -> {
                        boolean isPlayer = entity instanceof Player;
                        boolean[] isStaffUser = {false}; // cool hack!

                        if (isPlayer) {
                            Player player = (Player) entity;
                            getStaffUser(armorStand).ifPresent(
                                    staffUser -> isStaffUser[0] = player.getUniqueId() != staffUser.getUniqueId());
                        }

                        return isPlayer && isStaffUser[0];
                    })
                    .forEach(playerEntity -> {
                        Vector playerLocDir = playerEntity.getLocation().getDirection();

                        double x = playerLocDir.getX();
                        double z = playerLocDir.getZ();

                        playerEntity.setVelocity(new Vector(-x, 0.7, -z));
                    });
        });
    }

    private Optional<Player> getStaffUser(ArmorStand armorStand) {
        String staffUserId = armorStand.getPersistentDataContainer()
                .get(ColorStaffItem.ID_OF_USER_TAG, PersistentDataType.STRING);
        if (staffUserId == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(Bukkit.getPlayer(UUID.fromString(staffUserId)));
    }
}
