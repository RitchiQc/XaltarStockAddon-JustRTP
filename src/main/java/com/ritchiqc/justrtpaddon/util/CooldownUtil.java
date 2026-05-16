package com.ritchiqc.justrtpaddon.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownUtil {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long cooldownMillis;

    public CooldownUtil(long cooldownSeconds) {
        this.cooldownMillis = cooldownSeconds * 1000;
    }

    public boolean isOnCooldown(UUID playerId) {
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        long remaining = cooldowns.get(playerId) - System.currentTimeMillis();
        return remaining > 0;
    }

    public long getRemainingSeconds(UUID playerId) {
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }
        long remaining = cooldowns.get(playerId) - System.currentTimeMillis();
        return Math.max(0, (remaining + 999) / 1000);
    }

    public void setCooldown(UUID playerId) {
        cooldowns.put(playerId, System.currentTimeMillis() + cooldownMillis);
    }

    public void removeCooldown(UUID playerId) {
        cooldowns.remove(playerId);
    }
}
