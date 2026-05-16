package com.ritchiqc.justrtpaddon.util;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public class CommandRegistrar {

    private final Plugin plugin;
    private CommandMap commandMap;

    public CommandRegistrar(Plugin plugin) {
        this.plugin = plugin;
        initCommandMap();
    }

    private void initCommandMap() {
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                commandMap = (CommandMap) field.get(Bukkit.getPluginManager());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize CommandMap: " + e.getMessage());
        }
    }

    public void registerCommand(String name, String description, String usage, List<String> aliases,
                                 String permission, org.bukkit.command.CommandExecutor executor) {
        try {
            PluginCommand command = createPluginCommand(name);
            if (command == null) {
                plugin.getLogger().warning("Failed to create command: " + name);
                return;
            }

            command.setDescription(description);
            command.setUsage(usage);
            command.setPermission(permission);
            if (aliases != null) {
                command.setAliases(aliases);
            }
            command.setExecutor(executor);

            commandMap.register(plugin.getName().toLowerCase(), command);
            plugin.getLogger().info("Registered command: /" + name);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register command " + name + ": " + e.getMessage());
        }
    }

    private PluginCommand createPluginCommand(String name) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, plugin);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create PluginCommand: " + e.getMessage());
            return null;
        }
    }
}
