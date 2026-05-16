package com.ritchiqc.justrtpaddon.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Set;

public class CommandSendListener implements Listener {

    private static final Set<String> COMMANDS = Set.of("rtpstock", "rtpgive", "rtpstaff");

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        event.getCommands().addAll(COMMANDS);
    }
}
