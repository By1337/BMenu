package dev.by1337.bmenu.slot.component;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.handler.LegacyHandler;
import dev.by1337.bmenu.handler.RequirementHandler;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.jetbrains.annotations.Nullable;

public class OnViewComponent {
    public static final OnViewComponent EMPTY = new OnViewComponent(null, null);
    public static YamlCodec<OnViewComponent> CODEC = RecordYamlCodecBuilder.mapOf(
                    OnViewComponent::new,
                    RequirementHandler.CODEC.fieldOf("requirements", OnViewComponent::requirement),
                    Commands.CODEC.fieldOf("deny_commands", OnViewComponent::denyCommands)
            ).schema(s -> s.or(SchemaTypes.STRING))
            .whenPrimitive(RequirementHandler.CODEC.map(
                    r -> new OnViewComponent(r, null),
                    v -> v.requirement
            ));

    private final @Nullable RequirementHandler requirement;
    private final @Nullable Commands denyCommands;

    public OnViewComponent(@Nullable RequirementHandler requirement, @Nullable Commands denyCommands) {
        this.requirement = requirement;
        this.denyCommands = denyCommands;
    }

    public boolean isVisible(Menu menu, SlotContent slotContent) {
        if (requirement == null) return true;
        var placeholders = slotContent.getPlaceholders(menu);
        if (requirement instanceof LegacyHandler lh) {
            var ctx = ExecuteContext.of(menu, slotContent);
            var bool = lh.test(ctx, placeholders);
            if (!bool && denyCommands != null) denyCommands.run(ctx, placeholders);
            return bool;
        } else {
            boolean bool = requirement.test(menu, placeholders);
            if (!bool && denyCommands != null) denyCommands.run(ExecuteContext.of(menu, slotContent), placeholders);
            return bool;
        }
    }

    public RequirementHandler requirement() {
        return requirement;
    }

    public Commands denyCommands() {
        return denyCommands;
    }

    public boolean isEmpty() {
        return requirement == null;
    }
}
