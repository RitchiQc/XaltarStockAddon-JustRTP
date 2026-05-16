package com.ritchiqc.justrtpaddon.config;

import com.ritchiqc.justrtpaddon.XaltarStockAddon;
import com.ritchiqc.justrtpaddon.util.ConfigUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class AddonConfig {

    private final XaltarStockAddon addon;
    private final ConfigUtil configUtil;
    private FileConfiguration config;
    private String language;
    private long cooldownSeconds;
    private int firstJoinAmount;
    private final Map<String, Integer> limits = new HashMap<>();

    public AddonConfig(XaltarStockAddon addon) {
        this.addon = addon;
        this.configUtil = new ConfigUtil(
                addon.getDataFolder(),
                XaltarStockAddon.class,
                addon.getLogger()
        );
        load();
    }

    public void load() {
        configUtil.saveDefaultResource("config.yml");
        config = configUtil.loadYamlConfig("config.yml");

        language = config.getString("language", "fr");
        cooldownSeconds = config.getLong("cooldown-seconds", 5);
        firstJoinAmount = config.getInt("first-join-amount", 2);

        limits.clear();
        if (config.contains("limits")) {
            for (String key : config.getConfigurationSection("limits").getKeys(false)) {
                limits.put(key, config.getInt("limits." + key, 5));
            }
        }

        if (limits.isEmpty()) {
            limits.put("justrtp.stock.default", 5);
            limits.put("justrtp.stock.vip", 10);
            limits.put("justrtp.stock.mvp", 20);
        }
    }

    public String getLanguage() {
        return language;
    }

    public long getCooldownSeconds() {
        return cooldownSeconds;
    }

    public int getFirstJoinAmount() {
        return firstJoinAmount;
    }

    public Map<String, Integer> getLimits() {
        return new HashMap<>(limits);
    }

    public int getLimitForPermission(String permission) {
        return limits.getOrDefault(permission, 5);
    }
}
