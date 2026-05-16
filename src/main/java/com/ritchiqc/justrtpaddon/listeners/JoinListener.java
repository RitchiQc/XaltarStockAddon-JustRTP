package com.ritchiqc.justrtpaddon.listeners;

import com.ritchiqc.justrtpaddon.XaltarStockAddon;
import com.ritchiqc.justrtpaddon.config.AddonConfig;
import com.ritchiqc.justrtpaddon.data.StockDataManager;
import com.ritchiqc.justrtpaddon.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final XaltarStockAddon addon;
    private final StockDataManager stockDataManager;
    private final AddonConfig addonConfig;
    private final MessageUtil messageUtil;

    public JoinListener(XaltarStockAddon addon) {
        this.addon = addon;
        this.stockDataManager = addon.getStockDataManager();
        this.addonConfig = addon.getAddonConfig();
        this.messageUtil = addon.getMessageUtil();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        stockDataManager.hasFirstJoined(player.getUniqueId()).thenAccept(hasJoined -> {
            if (!hasJoined) {
                int amount = addonConfig.getFirstJoinAmount();
                stockDataManager.setFirstJoined(player.getUniqueId()).thenRun(() -> {
                    stockDataManager.addStock(player.getUniqueId(), amount).thenRun(() -> {
                        addon.getPlugin().getFoliaScheduler().runNow(() -> {
                            messageUtil.send(player, "first-join", "%amount%", String.valueOf(amount));
                        });
                    });
                });
            }
        });
    }
}
