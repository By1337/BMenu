package dev.by1337.bmenu.command;

import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.random.WeightedItem;
import dev.by1337.bmenu.random.WeightedItemSelector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandList {
    public static final YamlCodec<CommandList> CODEC =
            YamlCodec.mapOf(YamlCodec.STRING, WeightedCommands.CODEC).map(CommandList::new, c -> c.commandByName);
    private final List<WeightedCommands> commands;
    private final Map<String, WeightedCommands> commandByName;
    private WeightedItemSelector<Commands> randSelector;

    public CommandList(Map<String, WeightedCommands> map) {
        commands = new ArrayList<>();
        commandByName = new HashMap<>();
        if (map.isEmpty()) return;
        for (String string : map.keySet()) {
            WeightedCommands commands1 = map.get(string);
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
    public Commands getRandom() {
        return randSelector == null ? null : randSelector.getRandomItem();
    }

    @Nullable
    public Commands getByName(String name) {
        return commandByName.getOrDefault(name, WeightedCommands.EMPTY).commands;
    }

    public static class WeightedCommands implements WeightedItem<Commands> {
        public static final YamlCodec<WeightedCommands> CODEC = RecordYamlCodecBuilder.mapOf(
                WeightedCommands::new,
                YamlCodec.DOUBLE.fieldOf("weight", WeightedCommands::weight, 1D),
                Commands.CODEC.fieldOf("commands", WeightedCommands::commands, Commands.EMPTY)
        );
        private static final WeightedCommands EMPTY = new WeightedCommands(0D, null);
        private final double weight;
        private final Commands commands;

        public WeightedCommands(double weight, Commands commands) {
            this.weight = weight;
            this.commands = commands;
        }

        @Override
        public Commands value() {
            return commands;
        }

        @Override
        public double weight() {
            return weight;
        }

        public Commands commands() {
            return commands;
        }
    }
}
