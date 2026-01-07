package dev.by1337.bmenu.item;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.requirement.Requirements;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;

public class ViewRequirement {
    public static final ViewRequirement EMPTY = new ViewRequirement(Requirements.EMPTY, Commands.EMPTY);
    public static YamlCodec<ViewRequirement> CODEC = RecordYamlCodecBuilder.mapOf(
            ViewRequirement::new,
            Requirements.CODEC.fieldOf("requirements", ViewRequirement::requirement, Requirements.EMPTY),
            Commands.CODEC.fieldOf("deny_commands", ViewRequirement::denyCommands, Commands.EMPTY)
    ).whenPrimitive(Requirements.CODEC.map(
            r -> new ViewRequirement(r, Commands.EMPTY),
            v -> v.requirement
    ));

    private final Requirements requirement;
    private final Commands denyCommands;

    public ViewRequirement(Requirements requirement, Commands denyCommands) {
        this.requirement = requirement;
        this.denyCommands = denyCommands;
    }

    public Requirements requirement() {
        return requirement;
    }

    public Commands denyCommands() {
        return denyCommands;
    }

    public boolean isEmpty() {
        return this == ViewRequirement.EMPTY || requirement.isEmpty();
    }
}
