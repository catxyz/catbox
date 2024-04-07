package me.cat.toybox.items;

import com.destroystokyo.paper.MaterialTags;
import com.google.common.base.Preconditions;
import me.cat.toybox.ToyboxPlugin;
import me.cat.toybox.helpers.LieDetectionHelper;
import me.cat.toybox.impl.abstraction.item.ToyboxItem;
import me.cat.toybox.impl.abstraction.item.ToyboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class TimeShifterItem extends ToyboxItem implements Listener {

    private static final NamespacedKey IS_CUSTOM_SIGN_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "is_custom_sign",
            ToyboxPlugin.get()
    ));
    private static final NamespacedKey CUSTOM_SIGN_OWNER_UUID_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "custom_sign_owner_uuid",
            ToyboxPlugin.get()
    ));
    private final AtomicReference<BukkitTask> timeTask = new AtomicReference<>(null);
    private Location timeInputSignLocation;
    private int updateIntervalSecondsValue = 0;
    private boolean isToggled = false;
    private boolean someoneAlreadyUsingItem = false;

    public TimeShifterItem() {
        super(
                new ToyboxItemBuilder()
                        .useActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .itemId("time_shifter")
                        .useCooldown(Duration.ZERO)
                        .material(Material.CLOCK)
                        .displayName(Component.text("Time Shifter", NamedTextColor.DARK_RED))
                        .lore(List.of(
                                Component.empty(),
                                Component.text("About time!", NamedTextColor.GRAY)
                        ))
        );
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (someoneAlreadyUsingItem) {
            player.sendMessage(Component.text("Someone else is using this already!", NamedTextColor.RED));
            return;
        }

        World world = player.getWorld();
        if (isToggled) {
            isToggled = false;
            player.sendMessage(Component.text("Time shifting stopped!", NamedTextColor.YELLOW));
            cancelTimeTask(player, true);
        } else {
            if (setAndOpenedSignAt(player)) {
                isToggled = true;
                player.sendMessage(Component.text("Time is shifting!", NamedTextColor.GREEN));

                AtomicLong time = new AtomicLong(world.getFullTime());
                Bukkit.getServer().getScheduler().runTaskTimer(ToyboxPlugin.get(), (task) -> {
                    timeTask.set(task);

                    if (isUpdateIntervalSecondsValueSet()) {
                        time.getAndAdd(updateIntervalSecondsValue * 20L);
                        world.setFullTime(time.get());
                    }
                }, 0L, 1L);
            }
        }

    }

    private boolean setAndOpenedSignAt(Player player) {
        Location newSignLocation = player.getEyeLocation()
                .clone()
                .add(0.5d, 1.0d, 0.5d);
        if (newSignLocation.getBlock().getType() == Material.AIR) {
            newSignLocation.getBlock()
                    .setType(Material.OAK_SIGN);

            Sign sign = (Sign) newSignLocation.getBlock().getState();
            sign.getSide(Side.FRONT).line(1, Component.text('^', NamedTextColor.YELLOW));
            sign.getSide(Side.FRONT).line(2, Component.text("Update interval", NamedTextColor.GREEN));
            sign.getSide(Side.FRONT).line(3, Component.text("⚠ (seconds!) ⚠", NamedTextColor.YELLOW));

            PersistentDataContainer signPdc = sign.getPersistentDataContainer();
            signPdc.set(IS_CUSTOM_SIGN_TAG, PersistentDataType.BOOLEAN, true);
            signPdc.set(CUSTOM_SIGN_OWNER_UUID_TAG, PersistentDataType.STRING, player.getUniqueId().toString());

            sign.update();

            this.timeInputSignLocation = newSignLocation;

            Bukkit.getServer()
                    .getScheduler()
                    .runTaskLater(ToyboxPlugin.get(),
                            () -> player.openSign(sign, Side.FRONT), ((6500L * 20L) / 1000L) / 20L);
            someoneAlreadyUsingItem = true;

            return true;
        } else {
            player.sendMessage(Component.text("Make sure there are no blocks above your head!", NamedTextColor.RED));
            return false;
        }
    }

    @EventHandler
    public void onPlayerSignEdit(SignChangeEvent event) {
        Player player = event.getPlayer();

        Sign sign = null;
        if (isSignLocationValid()) {
            sign = (Sign) timeInputSignLocation.getBlock().getState(false);
        }

        PersistentDataContainer signPdc;
        if (sign != null) {
            signPdc = sign.getPersistentDataContainer();
        } else {
            return;
        }
        if (signPdc.has(IS_CUSTOM_SIGN_TAG) && signPdc.has(CUSTOM_SIGN_OWNER_UUID_TAG)) {
            String storedSignOwnerUuid = signPdc.get(CUSTOM_SIGN_OWNER_UUID_TAG, PersistentDataType.STRING);

            if (Objects.equals(storedSignOwnerUuid, player.getUniqueId().toString())) {
                Bukkit.getServer().getScheduler().runTaskLater(ToyboxPlugin.get(), () -> {
                    Sign updatedSign = (Sign) event.getBlock().getState(false);
                    updatedSign.update();
                    String firstSignLine = ((TextComponent) updatedSign.getSide(Side.FRONT).line(0)).content();

                    if (!firstSignLine.isEmpty() && LieDetectionHelper.isInt(firstSignLine)) {
                        this.updateIntervalSecondsValue = Integer.parseInt(firstSignLine);
                        if (updateIntervalSecondsValue <= 0) {
                            updateIntervalSecondsValue = 1;
                        }

                        player.sendMessage(Component.text("Value set to ", NamedTextColor.GREEN)
                                .append(Component.text(updateIntervalSecondsValue, NamedTextColor.YELLOW)));
                        someoneAlreadyUsingItem = false;

                        makeSignMagicallyDisappear();
                    } else {
                        player.sendMessage(Component.text("You lied! '", NamedTextColor.RED)
                                .append(Component.text(firstSignLine, NamedTextColor.YELLOW))
                                .append(Component.text("' is empty/not a number!", NamedTextColor.RED)));
                        someoneAlreadyUsingItem = false;

                        if (!isUpdateIntervalSecondsValueSet()) {
                            cancelTimeTask(null, false);
                        }
                        makeSignMagicallyDisappear();
                    }
                }, 5L);
            } else {
                player.sendMessage(Component.text("This is not your sign!", NamedTextColor.RED));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCustomSignBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (MaterialTags.SIGNS.isTagged(block)) {
            Sign sign = (Sign) block.getState(false);
            PersistentDataContainer signPdc = sign.getPersistentDataContainer();

            if (signPdc.has(IS_CUSTOM_SIGN_TAG)) {
                String storedSignOwnerUuid = signPdc.get(CUSTOM_SIGN_OWNER_UUID_TAG, PersistentDataType.STRING);

                if (!Objects.equals(storedSignOwnerUuid, player.getUniqueId().toString())) {
                    player.sendMessage(Component.text("You can't break this sign! It's not yours! >:-(", NamedTextColor.RED));
                    event.setCancelled(true);
                }
            }
        }
    }

    public boolean isSignLocationValid() {
        return timeInputSignLocation != null;
    }

    private boolean isUpdateIntervalSecondsValueSet() {
        return updateIntervalSecondsValue != 0;
    }

    private void makeSignMagicallyDisappear() {
        Bukkit.getServer()
                .getScheduler()
                .runTaskLater(ToyboxPlugin.get(),
                        () -> timeInputSignLocation.getBlock().setType(Material.AIR), 10L);
    }

    private void cancelTimeTask(Player player, boolean shouldSendErrorMessage) {
        if (timeTask.get() != null) {
            timeTask.get().cancel();
        } else {
            if (player != null && shouldSendErrorMessage) {
                player.sendMessage(Component.text("Error while shift-stopping time!", NamedTextColor.RED));
            }
        }
    }
}
