package me.cat.catbox.items.beta;

import com.destroystokyo.paper.MaterialTags;
import me.cat.catbox.helpers.LieDetectionHelper;
import me.cat.catbox.helpers.LoopHelper;
import me.cat.catbox.helpers.NamespaceHelper;
import me.cat.catbox.impl.abstraction.item.CatboxItem;
import me.cat.catbox.impl.abstraction.item.CatboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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

public class TimeShifterItem extends CatboxItem implements Listener {

    private static final NamespacedKey IS_CUSTOM_SIGN_TAG = NamespaceHelper.newSelfPluginTag("is_custom_sign");
    private static final NamespacedKey CUSTOM_SIGN_OWNER_UUID_TAG = NamespaceHelper.newSelfPluginTag("custom_sign_owner_uuid");
    private final AtomicReference<BukkitTask> timeTask = new AtomicReference<>(null);
    private Location timeInputSignLocation;
    private int updateIntervalSecondsValue = 0;
    private boolean isToggled = false;
    private boolean someoneAlreadyUsingItem = false;

    public TimeShifterItem() {
        super(
                new CatboxItemBuilder()
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
                        .markedAsBeta(true)
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
                LoopHelper.runIndefinitely(0L, 1L, (task) -> {
                    timeTask.set(task);

                    if (isUpdateIntervalSecondsValueSet()) {
                        time.getAndAdd(updateIntervalSecondsValue * 20L);
                        world.setFullTime(time.get());
                    }
                });
            }
        }

    }

    private boolean setAndOpenedSignAt(Player player) {
        Location newSignLocation = player.getEyeLocation()
                .clone()
                .add(0.5, 1, 0.5);
        if (newSignLocation.getBlock().getType() == Material.AIR) {
            newSignLocation.getBlock()
                    .setType(Material.OAK_SIGN);

            Sign sign = (Sign) newSignLocation.getBlock().getState();
            sign.getSide(Side.FRONT).line(1, Component.text('^', NamedTextColor.YELLOW));
            sign.getSide(Side.FRONT).line(2, Component.text("Update interval", NamedTextColor.GREEN));
            sign.getSide(Side.FRONT).line(3, Component.text("-> (seconds!) <-", NamedTextColor.YELLOW));

            PersistentDataContainer signPdc = sign.getPersistentDataContainer();
            signPdc.set(IS_CUSTOM_SIGN_TAG, PersistentDataType.BOOLEAN, true);
            signPdc.set(CUSTOM_SIGN_OWNER_UUID_TAG, PersistentDataType.STRING, player.getUniqueId().toString());

            sign.update();

            this.timeInputSignLocation = newSignLocation;

            long delay = ((6_500L * 20L) / 1_000L) / 20L;
            LoopHelper.runAfter(delay, (task) -> player.openSign(sign, Side.FRONT));
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
                LoopHelper.runAfter(5L, (task) -> {
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
                });
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
        LoopHelper.runAfter(10L, (task) -> timeInputSignLocation.getBlock().setType(Material.AIR));
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
