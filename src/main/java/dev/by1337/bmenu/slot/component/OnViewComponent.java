package dev.by1337.bmenu.slot.component;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.handler.LegacyHandler;
import dev.by1337.bmenu.handler.RequirementHandler;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.Nullable;

public record OnViewComponent(@Nullable RequirementHandler requirement) {
    public static final OnViewComponent EMPTY = new OnViewComponent(null);
    public static YamlCodec<OnViewComponent> CODEC = RequirementHandler.CODEC.map(
            OnViewComponent::new,
            OnViewComponent::requirement
    );

    public boolean isVisible(Menu menu, SlotContent slotContent) {
        if (requirement == null) return true;
        var placeholders = slotContent.getPlaceholders(menu);
        if (requirement instanceof LegacyHandler lh) {
            return lh.test(ExecuteContext.of(menu, slotContent), placeholders);
        } else {
            return requirement.test(menu, placeholders);
        }
    }

    @Override
    public RequirementHandler requirement() {
        return requirement;
    }

    public boolean isEmpty() {
        return requirement == null;
    }
}
