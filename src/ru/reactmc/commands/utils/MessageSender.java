package ru.reactmc.commands.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageSender {

    public static void send(CommandSender sender, String message) {
        if(!(message == null || message.isEmpty()))
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}
