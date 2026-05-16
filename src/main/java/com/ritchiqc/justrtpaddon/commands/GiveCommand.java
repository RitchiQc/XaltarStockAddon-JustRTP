package com.ritchiqc.justrtpaddon.commands;

import com.ritchiqc.justrtpaddon.XaltarStockAddon;
import com.ritchiqc.justrtpaddon.data.StockDataManager;
import com.ritchiqc.justrtpaddon.util.CooldownUtil;
import com.ritchiqc.justrtpaddon.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveCommand implements CommandExecutor {

    private final XaltarStockAddon addon;
    private final StockDataManager stockDataManager;
    private final MessageUtil messageUtil;
    private final CooldownUtil cooldownUtil;

    public GiveCommand(XaltarStockAddon addon) {
        this.addon = addon;
        this.stockDataManager = addon.getStockDataManager();
        this.messageUtil = addon.getMessageUtil();
        this.cooldownUtil = addon.getCooldownUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messageUtil.send(sender, "player-only");
            return true;
        }

        if (!player.hasPermission("justrtp.stock.give")) {
            messageUtil.send(player, "no-permission");
            return true;
        }

        if (args.length != 2) {
            messageUtil.send(player, "usage-give");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            messageUtil.send(player, "give-invalid-amount");
            return true;
        }

        if (amount <= 0) {
            messageUtil.send(player, "give-invalid-amount");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            messageUtil.send(player, "give-target-offline", "%target%", args[1]);
            return true;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            messageUtil.send(player, "give-self");
            return true;
        }

        if (cooldownUtil.isOnCooldown(player.getUniqueId())) {
            long remaining = cooldownUtil.getRemainingSeconds(player.getUniqueId());
            messageUtil.send(player, "give-cooldown", "%seconds%", String.valueOf(remaining));
            return true;
        }

        stockDataManager.getStockSafe(player).thenAccept(senderStock -> {
            if (senderStock < amount) {
                addon.getPlugin().getFoliaScheduler().runNow(() -> {
                    messageUtil.send(player, "give-not-enough");
                });
                return;
            }

            stockDataManager.getRemainingSpace(target).thenAccept(remainingSpace -> {
                if (remainingSpace < amount) {
                    addon.getPlugin().getFoliaScheduler().runNow(() -> {
                        messageUtil.send(player, "give-target-full", "%target%", target.getName());
                    });
                    return;
                }

                stockDataManager.removeStock(player.getUniqueId(), amount).thenRun(() -> {
                    stockDataManager.addStock(target.getUniqueId(), amount).thenRun(() -> {
                        addon.getPlugin().getFoliaScheduler().runNow(() -> {
                            cooldownUtil.setCooldown(player.getUniqueId());
                            messageUtil.send(player, "give-success", "%amount%", String.valueOf(amount), "%target%", target.getName());
                            messageUtil.send(target, "give-received", "%player%", player.getName(), "%amount%", String.valueOf(amount));
                        });
                    });
                });
            });
        });

        return true;
    }
}
