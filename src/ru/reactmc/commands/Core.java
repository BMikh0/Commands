package ru.reactmc.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;
import ru.reactmc.commands.command.CommandManager;
import ru.reactmc.commands.files.Config;
import ru.reactmc.commands.listeners.CommandListener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Core extends JavaPlugin {

    private Config config;
    private Map<String, Command> serverCommands;

    @Override
    @SuppressWarnings("unchecked")
    public void onEnable() {
        this.config = new Config(this);

        boolean enableCustomCommands = config.getEnableCustomCommands();
        boolean enableBlockedCommands = config.getEnableBlockedCommands();

        if(enableBlockedCommands || enableCustomCommands) {
            try {
                Field field1 = getServer().getClass().getDeclaredField("commandMap");
                field1.setAccessible(true);
                SimpleCommandMap simpleCommandMap = (SimpleCommandMap) field1.get(getServer());

                Field field2 = simpleCommandMap.getClass().getDeclaredField("knownCommands");
                field2.setAccessible(true);
                Map<String, Command> knownCommands = (Map<String, Command>) field2.get(simpleCommandMap);
                Map<String, Command> temp = new HashMap<>(knownCommands);

                Method method = getServer().getClass().getDeclaredMethod("setVanillaCommands", boolean.class);
                method.setAccessible(true);
                method.invoke(getServer(), false);

                this.serverCommands = knownCommands;

                field2.set(simpleCommandMap, temp);
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }

            if(config.getEnableCustomCommands()) new CommandManager(this);

            if (config.getEnableBlockedCommands()) new CommandListener(this);

            config.save();
        } else Bukkit.getPluginManager().disablePlugin(this);
    }

    public Command getServerCommand(String name) {
        return serverCommands.get(name);
    }

    public Config getPluginConfig() {
        return config;
    }

}
