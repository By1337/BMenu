package org.by1337.bmenu.command;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.by1337.bmenu.factory.MenuCodecs;
import org.by1337.bmenu.random.WeightedItem;
import org.by1337.bmenu.random.WeightedItemSelector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandList {
    public static final YamlCodec<CommandList> CODEC =
            YamlCodec.mapOf(YamlCodec.STRING, Commands.CODEC).map(CommandList::new, c -> c.commandByName);
    private final List<Commands> commands;
    private final Map<String, Commands> commandByName;
    private WeightedItemSelector<List<String>> randSelector;

    public CommandList(Map<String, Commands> map) {
        commands = new ArrayList<>();
        commandByName = new HashMap<>();
        if (map.isEmpty()) return;
        for (String string : map.keySet()) {
            Commands commands1 = map.get(string);
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
        public static final YamlCodec<Commands> CODEC = RecordYamlCodecBuilder.mapOf(
                Commands::new,
                YamlCodec.DOUBLE.fieldOf("weight", Commands::weight, 1D),
                MenuCodecs.COMMANDS.fieldOf("commands", Commands::commands, List.of())
        );
        private static final Commands EMPTY = new Commands(0D, null);
        private final double weight;
        private final List<String> commands;

        public Commands(double weight, List<String> commands) {
            this.weight = weight;
            this.commands = commands;
        }

        @Override
        public List<String> value() {
            return commands;
        }

        @Override
        public double weight() {
            return weight;
        }

        public List<String> commands() {
            return commands;
        }
    }
}
