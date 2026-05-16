package com.ritchiqc.justrtpaddon.listeners;

import com.ritchiqc.justrtpaddon.XaltarStockAddon;
import com.ritchiqc.justrtpaddon.data.StockDataManager;
import com.ritchiqc.justrtpaddon.util.MessageUtil;
import eu.kotori.justRTP.events.PlayerPostRTPEvent;
import eu.kotori.justRTP.events.PlayerRTPEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RTPStockListener implements Listener {

    private final XaltarStockAddon addon;
    private final StockDataManager stockDataManager;
    private final MessageUtil messageUtil;
    private final Set<UUID> freeRTPPlayers = ConcurrentHashMap.newKeySet();

    public RTPStockListener(XaltarStockAddon addon) {
        this.addon = addon;
        this.stockDataManager = addon.getStockDataManager();
        this.messageUtil = addon.getMessageUtil();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRTP(PlayerRTPEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Remove any stale entry
        freeRTPPlayers.remove(playerId);

        stockDataManager.getStockSafe(player).thenAccept(stock -> {
            if (stock > 0) {
                freeRTPPlayers.add(playerId);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPostRTP(PlayerPostRTPEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!freeRTPPlayers.remove(playerId)) {
            return;
        }

        double cost = event.getCost();

        stockDataManager.removeStock(playerId, 1).thenRun(() -> {
            addon.getPlugin().getFoliaScheduler().runNow(() -> {
                // Refund the cost via Vault if economy is enabled
                if (cost > 0 && addon.getPlugin().getVaultHook() != null && addon.getPlugin().getVaultHook().hasEconomy()) {
                    addon.getPlugin().getVaultHook().depositPlayer(player, cost);
                }

                stockDataManager.getStock(playerId).thenAccept(remaining -> {
                    addon.getPlugin().getFoliaScheduler().runNow(() -> {
                        messageUtil.send(player, "rtp-used-free", "%remaining%", String.valueOf(remaining));
                    });
                });
            });
        });
    }
}
