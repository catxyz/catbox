package me.cat.itemsplugin.abstractitems.manager;

import com.google.common.collect.Maps;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Long> cooldowns;

    public CooldownManager() {
        this.cooldowns = Maps.newHashMap();
    }

    public void addToCooldown(UUID playerId) {
        if (!cooldowns.containsKey(playerId)) {
            cooldowns.put(playerId, System.currentTimeMillis());
        }
    }

    public void removeFromCooldown(UUID playerId) {
        cooldowns.remove(playerId);
    }

    public boolean isCooldownOver(UUID playerId, Duration providedCooldownDuration) {
        Long lastActionMillis = cooldowns.get(playerId);
        if (lastActionMillis == null) {
            return true;
        }
        long timeSinceLastAction = System.currentTimeMillis() - lastActionMillis;
        return timeSinceLastAction >= providedCooldownDuration.toMillis();
    }

    public Map<UUID, Long> getCooldowns() {
        return cooldowns;
    }
}
