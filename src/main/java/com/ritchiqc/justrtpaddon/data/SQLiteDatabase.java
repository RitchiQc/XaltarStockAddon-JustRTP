package com.ritchiqc.justrtpaddon.data;

import com.ritchiqc.justrtpaddon.XaltarStockAddon;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLiteDatabase implements Database {

    private final XaltarStockAddon addon;
    private Connection connection;
    private ExecutorService executor;

    public SQLiteDatabase(XaltarStockAddon addon) {
        this.addon = addon;
    }

    @Override
    public void connect() {
        try {
            File dataFolder = addon.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File dbFile = new File(dataFolder, "database.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            connection = DriverManager.getConnection(url);
            connection.setAutoCommit(true);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA synchronous=NORMAL;");
            }

            createTables();
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "XaltarStockAddon-SQLite");
                t.setDaemon(true);
                return t;
            });

            addon.getLogger().info("SQLite database connected successfully.");
        } catch (SQLException e) {
            addon.getLogger().severe("Failed to connect to SQLite database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnect() {
        if (executor != null) {
            executor.shutdown();
        }
        if (connection != null) {
            try {
                connection.close();
                addon.getLogger().info("SQLite database disconnected.");
            } catch (SQLException e) {
                addon.getLogger().warning("Error closing SQLite connection: " + e.getMessage());
            }
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS xaltar_stock_addon (
                    uuid TEXT PRIMARY KEY,
                    stock INTEGER NOT NULL DEFAULT 0,
                    first_joined INTEGER NOT NULL DEFAULT 0
                )
                """);
        }
    }

    @Override
    public CompletableFuture<Integer> getStock(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT stock FROM xaltar_stock_addon WHERE uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("stock");
                    }
                    return 0;
                }
            } catch (SQLException e) {
                addon.getLogger().warning("SQLite getStock error: " + e.getMessage());
                return 0;
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> setStock(UUID playerId, int amount) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO xaltar_stock_addon (uuid, stock, first_joined)
                VALUES (?, ?, 0)
                ON CONFLICT(uuid) DO UPDATE SET stock = excluded.stock
                """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerId.toString());
                ps.setInt(2, Math.max(0, amount));
                ps.executeUpdate();
            } catch (SQLException e) {
                addon.getLogger().warning("SQLite setStock error: " + e.getMessage());
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Boolean> hasFirstJoined(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT first_joined FROM xaltar_stock_addon WHERE uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("first_joined") == 1;
                    }
                    return false;
                }
            } catch (SQLException e) {
                addon.getLogger().warning("SQLite hasFirstJoined error: " + e.getMessage());
                return false;
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> setFirstJoined(UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO xaltar_stock_addon (uuid, stock, first_joined)
                VALUES (?, 0, 1)
                ON CONFLICT(uuid) DO UPDATE SET first_joined = 1
                """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerId.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                addon.getLogger().warning("SQLite setFirstJoined error: " + e.getMessage());
            }
        }, executor);
    }
}
