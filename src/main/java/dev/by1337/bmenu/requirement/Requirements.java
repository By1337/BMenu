package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.event.MenuEventHandler;
import dev.by1337.bmenu.requirement.legacy.RequirementsFactory;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Requirements {
    public static final YamlCodec<Requirements> CODEC = new YamlCodec<>() {
        final YamlCodec<List<LegacyRequirement>> LIST_CODEC = LegacyRequirement.CODEC.listOf();

        @Override
        public DataResult<Requirements> decode(YamlValue yamlValue) {
            if (yamlValue.isMap()) return DataResult.success(RequirementsFactory.readLegacy(yamlValue));
            return yamlValue.decode(LIST_CODEC).mapValue(Requirements::new);
        }

        @Override
        public YamlValue encode(Requirements requirements) {
            return LIST_CODEC.encode(requirements.getRequirements());
        }

        @Override
        public @NotNull SchemaType schema() {
            return LIST_CODEC.schema();
        }
    };

    public static final Requirements EMPTY = new Requirements(Collections.emptyList());
    private final List<LegacyRequirement> requirements;

    public Requirements(List<LegacyRequirement> requirements) {
        this.requirements = requirements;
    }

    public boolean test(Menu menu, PlaceholderApplier placeholders, ExecuteContext ctx) {
        boolean result = true;
        for (LegacyRequirement requirement : requirements) {
            try {
                if (!requirement.test(ctx, placeholders)) {
                    Commands c = requirement.denyCommands();
                    c.run(ctx, placeholders);
                    if (c.isHasBreak()) return false;
                    result = false;
                } else {
                    Commands c = requirement.commands();
                    c.run(ctx, placeholders);
                    if (c.isHasBreak()) return true;
                }
            } catch (Exception e) {
                menu.getLoader().getLogger().error("Failed to check requirement: {}", requirement, e);
            }
        }
        return result;
    }

    public List<LegacyRequirement> getRequirements() {
        return requirements;
    }


    public static Requirements empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return this == EMPTY || requirements.isEmpty();
    }
}