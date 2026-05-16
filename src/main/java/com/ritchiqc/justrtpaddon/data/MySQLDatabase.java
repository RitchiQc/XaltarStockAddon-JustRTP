package com.ritchiqc.justrtpaddon.data;

import com.ritchiqc.justrtpaddon.XaltarStockAddon;
import com.ritchiqc.justrtpaddon.config.AddonConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MySQLDatabase implements Database {

    private final XaltarStockAddon addon;
    private final AddonConfig config;
    private HikariDataSource dataSource;
    private ExecutorService executor;

    public MySQLDatabase(XaltarStockAddon addon, AddonConfig config) {
        this.addon = addon;
        this.config = config;
    }

    @Override
    public void connect() {
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC",
                    config.getMysqlHost(), config.getMysqlPort(), config.getMysqlDatabase()));
            hikariConfig.setUsername(config.getMysqlUsername());
            hikariConfig.setPassword(config.getMysqlPassword());
            hikariConfig.setMaximumPoolSize(config.getMysqlPoolSize());
            hikariConfig.setConnectionTimeout(config.getMysqlConnectionTimeout());
            hikariConfig.setPoolName("XaltarStockAddon-MySQL");

            dataSource = new HikariDataSource(hikariConfig);

            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS xaltar_stock_addon (
                        uuid VARCHAR(36) PRIMARY KEY,
                        stock INT NOT NULL DEFAULT 0,
                        first_joined BOOLEAN NOT NULL DEFAULT FALSE
                    )
                    """);
            }

            executor = Executors.newFixedThreadPool(config.getMysqlPoolSize(), r -> {
                Thread t = new Thread(r, "XaltarStockAddon-MySQL");
                t.setDaemon(true);
                return t;
            });

            addon.getLogger().info("MySQL database connected successfully.");
        } catch (SQLException e) {
            addon.getLogger().severe("Failed to connect to MySQL database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnect() {
        if (executor != null) {
            executor.shutdown();
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            addon.getLogger().info("MySQL database disconnected.");
        }
    }

    @Override
    public CompletableFuture<Integer> getStock(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT stock FROM xaltar_stock_addon WHERE uuid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("stock");
                    }
                    return 0;
                }
            } catch (SQLException e) {
                addon.getLogger().warning("MySQL getStock error: " + e.getMessage());
                return 0;
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> setStock(UUID playerId, int amount) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO xaltar_stock_addon (uuid, stock, first_joined)
                VALUES (?, ?, FALSE)
                ON DUPLICATE KEY UPDATE stock = VALUES(stock)
                """;
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerId.toString());
                ps.setInt(2, Math.max(0, amount));
                ps.executeUpdate();
            } catch (SQLException e) {
                addon.getLogger().warning("MySQL setStock error: " + e.getMessage());
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Boolean> hasFirstJoined(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT first_joined FROM xaltar_stock_addon WHERE uuid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("first_joined");
                    }
                    return false;
                }
            } catch (SQLException e) {
                addon.getLogger().warning("MySQL hasFirstJoined error: " + e.getMessage());
                return false;
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> setFirstJoined(UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO xaltar_stock_addon (uuid, stock, first_joined)
                VALUES (?, 0, TRUE)
                ON DUPLICATE KEY UPDATE first_joined = TRUE
                """;
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerId.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                addon.getLogger().warning("MySQL setFirstJoined error: " + e.getMessage());
            }
        }, executor);
    }
}
