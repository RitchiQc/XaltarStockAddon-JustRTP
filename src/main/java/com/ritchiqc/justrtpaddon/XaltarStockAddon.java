package com.ritchiqc.justrtpaddon;

import com.ritchiqc.justrtpaddon.commands.GiveCommand;
import com.ritchiqc.justrtpaddon.commands.StaffCommand;
import com.ritchiqc.justrtpaddon.commands.StockCommand;
import com.ritchiqc.justrtpaddon.config.AddonConfig;
import com.ritchiqc.justrtpaddon.data.StockDataManager;
import com.ritchiqc.justrtpaddon.listeners.CommandSendListener;
import com.ritchiqc.justrtpaddon.listeners.JoinListener;
import com.ritchiqc.justrtpaddon.listeners.RTPStockListener;
import com.ritchiqc.justrtpaddon.placeholder.StockPlaceholderExpansion;
import com.ritchiqc.justrtpaddon.util.CommandRegistrar;
import com.ritchiqc.justrtpaddon.util.ConfigUtil;
import com.ritchiqc.justrtpaddon.util.CooldownUtil;
import com.ritchiqc.justrtpaddon.util.MessageUtil;
import eu.kotori.justRTP.addons.JustRTPAddon;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;


public class XaltarStockAddon extends JustRTPAddon {

    private static XaltarStockAddon instance;
    private AddonConfig addonConfig;
    private StockDataManager stockDataManager;
    private MessageUtil messageUtil;
    private CooldownUtil cooldownUtil;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Enabling XaltarStockAddon-JustRTP v" + getVersion());

        // Load config
        this.addonConfig = new AddonConfig(this);

        // Load messages
        String lang = addonConfig.getLanguage();
        ConfigUtil configUtil = new ConfigUtil(
                getDataFolder(),
                XaltarStockAddon.class,
                getLogger()
        );
        configUtil.saveDefaultResource("lang/fr.yml");
        configUtil.saveDefaultResource("lang/en.yml");
        FileConfiguration messages = configUtil.loadYamlConfig("lang/" + lang + ".yml");
        this.messageUtil = new MessageUtil(messages);

        // Init managers
        this.stockDataManager = new StockDataManager(this, addonConfig);
        this.cooldownUtil = new CooldownUtil(addonConfig.getCooldownSeconds());

        // Register commands dynamically
        CommandRegistrar commandRegistrar = new CommandRegistrar(getPlugin());
        commandRegistrar.registerCommand("rtpstock", "Voir son stock de RTP gratuits", "/rtpstock", null, "justrtp.stock.use", new StockCommand(this));
        commandRegistrar.registerCommand("rtpgive", "Envoyer des RTP a un joueur", "/rtpgive <nombre> <joueur>", null, "justrtp.stock.give", new GiveCommand(this));
        commandRegistrar.registerCommand("rtpstaff", "Gerer les RTP des joueurs", "/rtpstaff <stock|add|remove|set> <nombre> <joueur>", null, "justrtp.stock.admin", new StaffCommand(this));

        // Register listeners
        PluginManager pm = getPlugin().getServer().getPluginManager();
        pm.registerEvents(new RTPStockListener(this), getPlugin());
        pm.registerEvents(new JoinListener(this), getPlugin());
        pm.registerEvents(new CommandSendListener(), getPlugin());

        // Force command tree update for online players so new commands appear immediately
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.updateCommands();
        }

        // Register PlaceholderAPI expansion
        if (pm.getPlugin("PlaceholderAPI") != null) {
            new StockPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered!");
        }

        getLogger().info("XaltarStockAddon-JustRTP enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling XaltarStockAddon-JustRTP...");
        instance = null;
    }

    public static XaltarStockAddon getInstance() {
        return instance;
    }

    public AddonConfig getAddonConfig() {
        return addonConfig;
    }

    public StockDataManager getStockDataManager() {
        return stockDataManager;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public CooldownUtil getCooldownUtil() {
        return cooldownUtil;
    }
}
