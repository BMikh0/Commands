package ru.reactmc.commands.command;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import ru.reactmc.commands.Core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static ru.reactmc.commands.utils.MessageSender.send;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final Core core;
    private final Map<String, CustomCommand> commands;

    public CommandManager(Core core) {
        this.core = core;
        this.commands = new HashMap<>();

        List<CustomCommand> commands = core.getPluginConfig().parseCommands();

        if(!(commands == null || commands.isEmpty())) commands.forEach(this::register);
    }

    private void register(CustomCommand command) {
        if(command == null) return;

        try {
            Constructor constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = (PluginCommand) constructor.newInstance(command.getName(), core);

            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getServer());

            commandMap.register(core.getDescription().getName(), initPluginCommand(command, pluginCommand));

            pluginCommand.setExecutor(this);

            commands.put(command.getName(), command);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private PluginCommand initPluginCommand(CustomCommand command, PluginCommand pluginCommand) {
        pluginCommand.setUsage(command.getUsage());
        pluginCommand.setPermissionMessage(core.getPluginConfig().getNoPermissionMessage());

        if(command.haveDescription()) pluginCommand.setDescription(command.getDescription());
        if(command.havePermission()) pluginCommand.setPermission(command.getPermission());
        if(command.haveAliases()) pluginCommand.setAliases(command.getAliases());

        return pluginCommand;
    }

    private CustomCommand getCustomCommand(String name) {
        CustomCommand command = commands.get(name);

        if(command == null) {
            for(CustomCommand cmd: commands.values())
                if(cmd.haveAliases() && cmd.getAliases().contains(name)) return cmd;
        }

        return command;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] args) {
        CustomCommand command = getCustomCommand(label.toLowerCase());
        String result = command.getSource();

        if(command.haveArgs()) {
            List<Argument> arguments = command.getArgs();
            int i = 0;

            try {
                for(i =  0; i < arguments.size(); i++) {
                    String arg = args[i];
                    Argument argument = arguments.get(i);

                    if(argument.haveAvailableValues() && !argument.getAvailableValues().contains(arg.toLowerCase())) {
                        send(commandSender, argument.haveFilterFailedMessage() ? argument.getFilterFailedMessage() : getUsageMessage(command));

                        return true;
                    }

                    result = result.replace("%"  + argument.getName() + "%", arg);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                for(; i < arguments.size(); i++) {
                    Argument argument = arguments.get(i);

                    if(argument.isRequired()) {
                        send(commandSender, argument.haveIfMissedMessage() ? argument.getIfMissedMessage() : getUsageMessage(command));

                        return true;
                    }
                }

            }
        }

        String permission = command.getSourceCommand().getPermission();

        if(!commandSender.hasPermission(permission)) {
            PermissionAttachment permissionAttachment = commandSender.addAttachment(core, permission, true);
            Bukkit.getServer().dispatchCommand(commandSender, result.replaceAll("%[A-Za-z0-9 ]*%", ""));
            commandSender.removeAttachment(permissionAttachment);
        } else
            Bukkit.getServer().dispatchCommand(commandSender, result.replaceAll("%[A-Za-z0-9 ]*%", ""));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command cmd, String label, String[] args) {
        CustomCommand command = getCustomCommand(label);

        if(command.haveArgs()) {
            List<String> arguments = new ArrayList<>();

            for(int i = 0; i < args.length; i++) {
                if(!args[i].isEmpty() || i == args.length - 1) arguments.add(args[i]);
            }

            args = arguments.toArray(new String[0]);

            try {
                List<String> values = command.getArgs().get(args.length-1).getTabCompleteValues();

                if(values != null) {
                    String arg = args[args.length-1].toLowerCase();

                    if(!arg.isEmpty())
                        return values.stream().filter(x -> x.startsWith(arg)).collect(Collectors.toList());
                    else return values;
                }
            } catch(IndexOutOfBoundsException ignored) {}
        }

        return null;
    }

    private String getUsageMessage(CustomCommand command) {
        String wrongUsage = core.getPluginConfig().getWrongUsageMessage(),
                usage = command.getUsage();

        return wrongUsage.replace("%usage%", usage);
    }

}
