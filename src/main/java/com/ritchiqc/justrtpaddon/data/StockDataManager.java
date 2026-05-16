package com.ritchiqc.justrtpaddon.data;

import com.ritchiqc.justrtpaddon.XaltarStockAddon;
import com.ritchiqc.justrtpaddon.config.AddonConfig;
import eu.kotori.justRTP.utils.DataManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StockDataManager {

    private final XaltarStockAddon addon;
    private final AddonConfig config;
    private static final String STOCK_PREFIX = "justrtp_addon_stock:";
    private static final String FIRST_JOIN_PREFIX = "justrtp_addon_firstjoin:";

    public StockDataManager(XaltarStockAddon addon, AddonConfig config) {
        this.addon = addon;
        this.config = config;
    }

    private DataManager getDataManager() {
        return addon.getPlugin().getDataManager();
    }

    public int getPlayerLimit(Player player) {
        int maxLimit = 0;
        Map<String, Integer> limits = config.getLimits();

        for (Map.Entry<String, Integer> entry : limits.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                maxLimit = Math.max(maxLimit, entry.getValue());
            }
        }

        return maxLimit > 0 ? maxLimit : config.getLimitForPermission("justrtp.stock.default");
    }

    public CompletableFuture<Integer> getStock(UUID playerId) {
        return getDataManager().getString(STOCK_PREFIX + playerId.toString())
                .thenApply(opt -> opt.map(Integer::parseInt).orElse(0));
    }

    public CompletableFuture<Void> setStock(UUID playerId, int amount) {
        return getDataManager().setString(STOCK_PREFIX + playerId.toString(), String.valueOf(Math.max(0, amount)));
    }

    public CompletableFuture<Void> addStock(UUID playerId, int amount) {
        return getStock(playerId).thenCompose(current -> {
            int newAmount = current + amount;
            return setStock(playerId, newAmount);
        });
    }

    public CompletableFuture<Void> removeStock(UUID playerId, int amount) {
        return getStock(playerId).thenCompose(current -> {
            int newAmount = Math.max(0, current - amount);
            return setStock(playerId, newAmount);
        });
    }

    public CompletableFuture<Boolean> hasFirstJoined(UUID playerId) {
        return getDataManager().getString(FIRST_JOIN_PREFIX + playerId.toString())
                .thenApply(Optional::isPresent);
    }

    public CompletableFuture<Void> setFirstJoined(UUID playerId) {
        return getDataManager().setString(FIRST_JOIN_PREFIX + playerId.toString(), "true");
    }

    public CompletableFuture<Integer> getStockSafe(Player player) {
        return getStock(player.getUniqueId());
    }

    public CompletableFuture<Boolean> canAddStock(Player player, int amount) {
        return getStock(player.getUniqueId()).thenApply(current -> {
            int limit = getPlayerLimit(player);
            return (current + amount) <= limit;
        });
    }

    public CompletableFuture<Integer> getRemainingSpace(Player player) {
        return getStock(player.getUniqueId()).thenApply(current -> {
            int limit = getPlayerLimit(player);
            return Math.max(0, limit - current);
        });
    }
}
