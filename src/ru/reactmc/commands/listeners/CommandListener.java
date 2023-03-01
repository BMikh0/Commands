package ru.reactmc.commands.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.reactmc.commands.Core;
import ru.reactmc.commands.utils.MessageSender;

import java.util.List;

public class CommandListener implements Listener {

    private final Core core;
    private final List<String> blockedCommands;

    public CommandListener(Core core) {
        this.core = core;
        this.blockedCommands = core.getPluginConfig().getBlockedCommands();

        if(blockedCommands != null) Bukkit.getPluginManager().registerEvents(this, core);
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onCommand(PlayerCommandPreprocessEvent event) {
        if(blockedCommands.contains(event.getMessage().toLowerCase().substring(1).split(" ")[0])) {
            event.setCancelled(true);

            MessageSender.send(event.getPlayer(), core.getPluginConfig().getBlockedCommandMessage());
        }
    }

}
