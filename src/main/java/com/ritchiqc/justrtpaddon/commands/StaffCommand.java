package com.ritchiqc.justrtpaddon.commands;

import com.ritchiqc.justrtpaddon.XaltarStockAddon;
import com.ritchiqc.justrtpaddon.data.StockDataManager;
import com.ritchiqc.justrtpaddon.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffCommand implements CommandExecutor {

    private final XaltarStockAddon addon;
    private final StockDataManager stockDataManager;
    private final MessageUtil messageUtil;

    public StaffCommand(XaltarStockAddon addon) {
        this.addon = addon;
        this.stockDataManager = addon.getStockDataManager();
        this.messageUtil = addon.getMessageUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justrtp.stock.admin")) {
            messageUtil.send(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            messageUtil.send(sender, "usage-staff");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("stock")) {
            if (args.length != 2) {
                messageUtil.send(sender, "usage-staff");
                return true;
            }
            return handleStock(sender, args[1]);
        }

        if (args.length != 3) {
            messageUtil.send(sender, "usage-staff");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            messageUtil.send(sender, "staff-invalid-amount");
            return true;
        }

        if (amount < 0) {
            messageUtil.send(sender, "staff-invalid-amount");
            return true;
        }

        String targetName = args[2];
        Player target = Bukkit.getPlayer(targetName);

        switch (subCommand) {
            case "add":
                return handleAdd(sender, target, targetName, amount);
            case "remove":
                return handleRemove(sender, target, targetName, amount);
            case "set":
                return handleSet(sender, target, targetName, amount);
            default:
                messageUtil.send(sender, "unknown-command");
                return true;
        }
    }

    private boolean handleStock(CommandSender sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            messageUtil.send(sender, "staff-target-offline", "%player%", targetName);
            return true;
        }

        stockDataManager.getStockSafe(target).thenAccept(stock -> {
            int limit = stockDataManager.getPlayerLimit(target);
            addon.getPlugin().getFoliaScheduler().runNow(() -> {
                messageUtil.send(sender, "staff-stock", "%player%", target.getName(), "%stock%", String.valueOf(stock), "%limit%", String.valueOf(limit));
            });
        });

        return true;
    }

    private boolean handleAdd(CommandSender sender, Player target, String targetName, int amount) {
        if (target == null || !target.isOnline()) {
            messageUtil.send(sender, "staff-target-offline", "%player%", targetName);
            return true;
        }

        stockDataManager.getRemainingSpace(target).thenAccept(remaining -> {
            if (remaining < amount) {
                int limit = stockDataManager.getPlayerLimit(target);
                addon.getPlugin().getFoliaScheduler().runNow(() -> {
                    messageUtil.send(sender, "staff-limit-reached", "%player%", target.getName(), "%limit%", String.valueOf(limit));
                });
                return;
            }

            stockDataManager.addStock(target.getUniqueId(), amount).thenRun(() -> {
                stockDataManager.getStockSafe(target).thenAccept(newStock -> {
                    int limit = stockDataManager.getPlayerLimit(target);
                    addon.getPlugin().getFoliaScheduler().runNow(() -> {
                        messageUtil.send(sender, "staff-add-success", "%player%", target.getName(), "%amount%", String.valueOf(amount), "%stock%", String.valueOf(newStock), "%limit%", String.valueOf(limit));
                    });
                });
            });
        });

        return true;
    }

    private boolean handleRemove(CommandSender sender, Player target, String targetName, int amount) {
        if (target == null || !target.isOnline()) {
            messageUtil.send(sender, "staff-target-offline", "%player%", targetName);
            return true;
        }

        stockDataManager.getStockSafe(target).thenAccept(currentStock -> {
            int toRemove = Math.min(amount, currentStock);
            stockDataManager.removeStock(target.getUniqueId(), toRemove).thenRun(() -> {
                stockDataManager.getStockSafe(target).thenAccept(newStock -> {
                    int limit = stockDataManager.getPlayerLimit(target);
                    addon.getPlugin().getFoliaScheduler().runNow(() -> {
                        messageUtil.send(sender, "staff-remove-success", "%player%", target.getName(), "%amount%", String.valueOf(toRemove), "%stock%", String.valueOf(newStock), "%limit%", String.valueOf(limit));
                    });
                });
            });
        });

        return true;
    }

    private boolean handleSet(CommandSender sender, Player target, String targetName, int amount) {
        if (target == null || !target.isOnline()) {
            messageUtil.send(sender, "staff-target-offline", "%player%", targetName);
            return true;
        }

        int limit = stockDataManager.getPlayerLimit(target);
        if (amount > limit) {
            messageUtil.send(sender, "staff-limit-reached", "%player%", target.getName(), "%limit%", String.valueOf(limit));
            return true;
        }

        stockDataManager.setStock(target.getUniqueId(), amount).thenRun(() -> {
            addon.getPlugin().getFoliaScheduler().runNow(() -> {
                messageUtil.send(sender, "staff-set-success", "%player%", target.getName(), "%amount%", String.valueOf(amount), "%limit%", String.valueOf(limit));
            });
        });

        return true;
    }
}
