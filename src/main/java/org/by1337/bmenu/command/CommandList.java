package org.by1337.bmenu.command;

import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.blib.random.WeightedItem;
import org.by1337.blib.random.WeightedItemSelector;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandList {
    private final List<Commands> commands;
    private final Map<String, Commands> commandByName;
    private WeightedItemSelector<List<String>> randSelector;

    public CommandList(YamlValue value) {
        if (value.getValue() == null) {
            commands = new ArrayList<>();
            commandByName = new HashMap<>();
            return;
        }
        Map<String, YamlContext> map = value.getAsMap(YamlContext.class);
        commands = new ArrayList<>();
        commandByName = new HashMap<>();
        for (String string : map.keySet()) {
            Commands commands1 = new Commands(map.get(string));
            commands.add(commands1);
            commandByName.put(string, commands1);
        }
        randSelector = new WeightedItemSelector<>(commands);
    }

    public void merge(CommandList other) {
        commands.addAll(other.commands);
        commandByName.putAll(other.commandByName);
        if (commands.isEmpty()) {
            randSelector = null;
        } else {
            randSelector = new WeightedItemSelector<>(commands);
        }
    }

    @Nullable
    public List<String> getRandom() {
        return randSelector == null ? null : randSelector.getRandomItem();
    }

    @Nullable
    public List<String> getByName(String name) {
        return commandByName.getOrDefault(name, Commands.EMPTY).commands;
    }

    public static class Commands implements WeightedItem<List<String>> {
        private static final Commands EMPTY = new Commands(0D, null);
        private final double weight;
        private final List<String> commands;

        public Commands(double weight, List<String> commands) {
            this.weight = weight;
            this.commands = commands;
        }

        public Commands(YamlContext ctx) {
            weight = ctx.getAsDouble("weight", 1D);
            commands = ctx.get("commands").getAsList(YamlValue::getAsString, Collections.emptyList());
        }

        @Override
        public List<String> value() {
            return commands;
        }

        @Override
        public double weight() {
            return weight;
        }
    }
}
