package me.cat.toybox.impl.managers;

import com.google.common.collect.Maps;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public class CooldownManager implements Listener {

    private final Map<UUID, Long> cooldowns;

    public CooldownManager() {
        this.cooldowns = Maps.newHashMap();
    }

    // k = player ID, v = current ms (start time)
    public void addToCooldown(UUID playerId) {
        cooldowns.putIfAbsent(playerId, System.currentTimeMillis());
    }

    public void removeFromCooldown(UUID playerId) {
        cooldowns.remove(playerId);
    }

    public boolean isCooldownOver(UUID playerId, Duration providedCooldownDuration) {
        Long startTime = cooldowns.get(playerId);
        if (startTime == null) {
            return true;
        }
        if (providedCooldownDuration.isNegative()
                || providedCooldownDuration.isZero()) {
            return true;
        }
        long futureExpirationMillis = startTime + providedCooldownDuration.toMillis();

        return System.currentTimeMillis() >= futureExpirationMillis;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        removeFromCooldown(playerId);
    }

    public Map<UUID, Long> getCooldowns() {
        return cooldowns;
    }
}
