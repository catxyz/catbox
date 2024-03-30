package me.cat.toybox.helpers;

import me.cat.toybox.ToyboxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class LoopHelper {

    public static void runAtNextTick(Consumer<BukkitTask> taskConsumer) {
        Bukkit.getServer()
                .getScheduler()
                .runTask(ToyboxPlugin.get(), taskConsumer);
    }

    public static void runAfter(long delay, Consumer<BukkitTask> taskConsumer) {
        if (delay < 0L) {
            delay = 0L;
        }

        Bukkit.getServer()
                .getScheduler()
                .runTaskLater(ToyboxPlugin.get(),
                        taskConsumer, delay);
    }

    public static void runIndefinitely(long delay, long period, Consumer<BukkitTask> taskConsumer) {
        if (delay < 0L) {
            delay = 0L;
        }
        if (period <= 0L) {
            period = 1L;
        }

        Bukkit.getServer()
                .getScheduler()
                .runTaskTimer(ToyboxPlugin.get(),
                        taskConsumer, delay, period);
    }
}
