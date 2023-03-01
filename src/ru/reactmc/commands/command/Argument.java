package ru.reactmc.commands.command;

import java.util.List;
import java.util.stream.Collectors;

public class Argument {

    private final String name;
    private final boolean required;
    private final List<String> availableValues, tabCompleteValues;
    private final String filterFailedMessage, ifMissedMessage;

    public Argument(String name, boolean required, List<String> availableValues, List<String> tabCompleteValues, String filterFailedMessage, String ifMissedMessage) {
        this.name = name.toLowerCase();
        this.required = required;
        this.availableValues = !(availableValues == null || availableValues.isEmpty()) ? availableValues.stream().map(String::toLowerCase).collect(Collectors.toList()) : null;
        this.tabCompleteValues = !(tabCompleteValues == null || tabCompleteValues.isEmpty()) ? tabCompleteValues.stream().map(String::toLowerCase).collect(Collectors.toList()) : null;
        this.filterFailedMessage = filterFailedMessage;
        this.ifMissedMessage = ifMissedMessage;
    }

    String getName() {
        return name;
    }

    boolean isRequired() {
        return required;
    }

    boolean haveAvailableValues() {
        return availableValues != null;
    }

    List<String> getAvailableValues() {
        return availableValues;
    }

    List<String> getTabCompleteValues() {
        return tabCompleteValues;
    }

    boolean haveFilterFailedMessage() {
        return filterFailedMessage != null;
    }

    String getFilterFailedMessage() {
        return filterFailedMessage;
    }

    boolean haveIfMissedMessage() {
        return ifMissedMessage != null;
    }

    String getIfMissedMessage() {
        return ifMissedMessage;
    }

}
