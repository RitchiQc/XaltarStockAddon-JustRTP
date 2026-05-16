package com.ritchiqc.justrtpaddon.data;

import com.ritchiqc.justrtpaddon.XaltarStockAddon;
import com.ritchiqc.justrtpaddon.config.AddonConfig;

public class DatabaseFactory {

    public static Database createDatabase(XaltarStockAddon addon, AddonConfig config) {
        String type = config.getStorageType();
        Database database;

        switch (type) {
            case "mysql" -> {
                database = new MySQLDatabase(addon, config);
                addon.getLogger().info("Using MySQL storage.");
            }
            case "sqlite" -> {
                database = new SQLiteDatabase(addon);
                addon.getLogger().info("Using SQLite storage.");
            }
            default -> {
                addon.getLogger().warning("Unknown storage type '" + type + "', defaulting to SQLite.");
                database = new SQLiteDatabase(addon);
            }
        }

        database.connect();
        return database;
    }
}
