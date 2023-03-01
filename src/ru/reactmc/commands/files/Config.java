package ru.reactmc.commands.files;

import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.reactmc.commands.Core;
import ru.reactmc.commands.command.Argument;
import ru.reactmc.commands.command.CustomCommand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Config {

    private final Core core;
    private final File file;
    private final FileConfiguration config;

    public Config(Core core) {
        this.core = core;

        core.saveDefaultConfig();

        this.file = new File(core.getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public List<CustomCommand> parseCommands() {
        ConfigurationSection customCommands;

        if((customCommands = config.getConfigurationSection("customCommands")) != null) {

            List<CustomCommand> commands = customCommands.getKeys(false)
                    .stream()
                    .map(cmd -> {
                        ConfigurationSection command = customCommands.getConfigurationSection(cmd);

                        if(command != null) {
                            Command sourceCommand;
                            String source = command.getString("source"),
                                    usage = get(command, "usage", "");

                            if(!(
                                    source == null
                                    || source.isEmpty()
                                    || (sourceCommand = core.getServerCommand(source.split(" ")[0])) == null
                            )) {
                                ConfigurationSection argsSection = command.getConfigurationSection("arguments");
                                List<Argument> args = null;

                                if(argsSection != null) {
                                    args = argsSection.getKeys(false).stream().map(arg -> {
                                        ConfigurationSection argument = argsSection.getConfigurationSection(arg);
                                        List<String> availableValues = argument.getStringList("availableValues");
                                        boolean required = get(argument, "required", false);

                                        return new Argument(
                                                arg,
                                                required,
                                                availableValues,
                                                argument.getStringList("tabCompleteValues"),
                                                !(availableValues == null || availableValues.isEmpty()) ?
                                                        argument.getString("filterFailedMessage") :
                                                        null,
                                                required ?
                                                        argument.getString("ifMissedMessage") :
                                                        null);
                                    }).collect(Collectors.toList());

                                } else
                                    set(command, "arguments", Collections.emptyList());

                                return new CustomCommand(
                                        cmd,
                                        source,
                                        sourceCommand,
                                        usage,
                                        get(command, "description", ""),
                                        command.getStringList("aliases"),
                                        get(command, "requiresPermission", false),
                                        args);
                            } else set(customCommands, cmd, null);
                        }

                        return null;
                    }).collect(Collectors.toList());

            return commands;
        } else {
            config.set("customCommands", Collections.emptyList());
        }

        return null;
    }

    private void set(ConfigurationSection section, String path, Object value) {
        config.set(section != null ? section.getCurrentPath() + "." + path : path, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T get(String path) {
        return (T) config.get(path);
    }

    private <T> T get(ConfigurationSection section, String path, T replacement) {
        if(section != null) path = section.getCurrentPath() + "." + path;

        T target = get(path);

        if(target == null) {
            config.set(path, replacement);
            return replacement;
        }

        return target;
    }

    private <T> T get(String path, T replacement) {
        return get(null, path, replacement);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getMessage(String path) {
        return get("messages." + path, "");
    }

    public String getBlockedCommandMessage() {
        return getMessage("blockedCommand");
    }

    public String getWrongUsageMessage() {
        return getMessage("wrongUsage");
    }

    public String getNoPermissionMessage() {
        return getMessage("noPermission");
    }

    public List<String> getBlockedCommands() {
        @SuppressWarnings("unchecked")
        List<String> list = get("blockedCommands", Collections.EMPTY_LIST);

        if(!list.isEmpty()) {
            boolean b = false;

            List<String> blockedCommands = new ArrayList<>();

            for(String s : list) {
                if(!(s == null || s.isEmpty() || core.getServerCommand(s = s.toLowerCase()) == null)) {
                    if(!b) b = true;

                    blockedCommands.add(s);
                }
            }

            if(b) set(null, "blockedCommands", blockedCommands);

            return blockedCommands;
        }

        return null;
    }

    public boolean getEnableCustomCommands() {
        return get("enableCustomCommands", true);
    }

    public boolean getEnableBlockedCommands() {
        return get("enableBlockedCommands", true);
    }

}
