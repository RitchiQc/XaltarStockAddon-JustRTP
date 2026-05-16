package com.ritchiqc.justrtpaddon.commands;

import com.ritchiqc.justrtpaddon.XaltarStockAddon;
import com.ritchiqc.justrtpaddon.data.StockDataManager;
import com.ritchiqc.justrtpaddon.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StockCommand implements CommandExecutor {

    private final XaltarStockAddon addon;
    private final StockDataManager stockDataManager;
    private final MessageUtil messageUtil;

    public StockCommand(XaltarStockAddon addon) {
        this.addon = addon;
        this.stockDataManager = addon.getStockDataManager();
        this.messageUtil = addon.getMessageUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messageUtil.send(sender, "player-only");
            return true;
        }

        if (!player.hasPermission("justrtp.stock.use")) {
            messageUtil.send(player, "no-permission");
            return true;
        }

        stockDataManager.getStockSafe(player).thenAccept(stock -> {
            int limit = stockDataManager.getPlayerLimit(player);
            addon.getPlugin().getFoliaScheduler().runNow(() -> {
                messageUtil.send(player, "stock-self", "%stock%", String.valueOf(stock), "%limit%", String.valueOf(limit));
            });
        });

        return true;
    }
}
