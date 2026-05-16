package com.ritchiqc.justrtpaddon.placeholder;

import com.ritchiqc.justrtpaddon.XaltarStockAddon;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StockPlaceholderExpansion extends PlaceholderExpansion {

    private final XaltarStockAddon addon;

    public StockPlaceholderExpansion(XaltarStockAddon addon) {
        this.addon = addon;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "justrtpstock";
    }

    @Override
    public @NotNull String getAuthor() {
        return addon.getPlugin().getDescription().getAuthors().isEmpty()
                ? "RitchiQc"
                : String.join(", ", addon.getPlugin().getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return addon.getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "0";
        }

        return switch (params.toLowerCase()) {
            case "stock" -> String.valueOf(addon.getStockDataManager().getStockSafe(player).join());
            case "limit" -> String.valueOf(addon.getStockDataManager().getPlayerLimit(player));
            case "remaining" -> String.valueOf(addon.getStockDataManager().getRemainingSpace(player).join());
            default -> null;
        };
    }
}
