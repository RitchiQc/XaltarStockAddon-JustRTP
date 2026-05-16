package com.ritchiqc.justrtpaddon.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageUtil {

    private final FileConfiguration messages;

    public MessageUtil(FileConfiguration messages) {
        this.messages = messages;
    }

    public String get(String key) {
        String prefix = messages.getString("prefix", "&8[&6RTP Stock&8] &r");
        String message = messages.getString(key, "&cMessage manquant: " + key);
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public String getRaw(String key) {
        String message = messages.getString(key, "&cMessage manquant: " + key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void send(CommandSender sender, String key) {
        sender.sendMessage(get(key));
    }

    public void send(CommandSender sender, String key, String... replacements) {
        String message = get(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        sender.sendMessage(message);
    }

    public void sendRaw(CommandSender sender, String key, String... replacements) {
        String message = getRaw(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        sender.sendMessage(message);
    }
}
