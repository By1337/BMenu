package dev.by1337.bmenu.requirement.legacy;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.handler.ConditionalHandler;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Requirements {
    public static final YamlCodec<Requirements> CODEC = new YamlCodec<>() {
        final YamlCodec<List<ConditionalHandler>> LIST_CODEC = ConditionalHandler.CODEC.listOf();

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
    private final List<ConditionalHandler> requirements;

    public Requirements(List<ConditionalHandler> requirements) {
        this.requirements = requirements;
    }

    public boolean test(Menu menu, PlaceholderApplier placeholders, ExecuteContext ctx) {
        boolean result = true;
        for (ConditionalHandler requirement : requirements) {
            try {
                if (!requirement.run(ctx, placeholders)) {
                    Commands c = requirement.elseCmds();
                    if (c.isHasBreak()) return false;
                    result = false;
                } else {
                    Commands c = requirement.doCmds();
                    if (c.isHasBreak()) return true;
                }
            } catch (Exception e) {
                menu.loader().logger().error("Failed to check requirement: {}", requirement, e);
            }
        }
        return result;
    }

    public List<ConditionalHandler> getRequirements() {
        return requirements;
    }


    public static Requirements empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return this == EMPTY || requirements.isEmpty();
    }

    @Override
    public String toString() {
        return "Requirements{" +
                "requirements=" + requirements +
                '}';
    }
}