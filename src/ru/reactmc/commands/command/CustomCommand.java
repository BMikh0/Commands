package ru.reactmc.commands.command;

import org.bukkit.command.Command;

import java.util.List;
import java.util.stream.Collectors;

public class CustomCommand {

    private final String name, usage, source, description, permission;
    private final Command sourceCommand;
    private final List<Argument> args;
    private final List<String> aliases;

    public CustomCommand(String name, String source, Command sourceCommand, String usage, String description,
                         List<String> aliases, boolean requiresPermission, List<Argument> args) {
        this.name = name.toLowerCase();
        this.source = source;
        this.sourceCommand = sourceCommand;
        this.usage = usage;
        this.description = description;
        this.permission = requiresPermission ? "commands." + name : null;
        this.args = !(args == null || args.isEmpty()) ? args : null;
        this.aliases = !(aliases == null || aliases.isEmpty()) ? aliases.stream().map(String::toLowerCase).collect(Collectors.toList()) : null;
    }

    String getName() {
        return name;
    }

    String getSource() {
        return source;
    }

    Command getSourceCommand() {
        return sourceCommand;
    }

    String getUsage() {
        return usage;
    }

    boolean haveDescription() {
        return description != null;
    }

    String getDescription() {
        return description;
    }

    boolean havePermission() {
        return permission != null;
    }

    String getPermission() {
        return permission;
    }

    boolean haveArgs() {
        return args != null;
    }

    List<Argument> getArgs() {
        return args;
    }

    boolean haveAliases() {
        return aliases != null;
    }

    List<String> getAliases() {
        return aliases;
    }

}
