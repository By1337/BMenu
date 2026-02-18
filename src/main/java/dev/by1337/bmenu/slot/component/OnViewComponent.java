package dev.by1337.bmenu.slot.component;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.Nullable;

public record OnViewComponent(@Nullable Requirement requirement) {
    public static final OnViewComponent EMPTY = new OnViewComponent(null);
    public static YamlCodec<OnViewComponent> CODEC = Requirement.CODEC.map(
            OnViewComponent::new,
            OnViewComponent::requirement
    );

    public boolean isVisible(Menu menu, SlotContent slotContent) {
        if (requirement == null) return true;
        var placeholders = slotContent.getPlaceholders(menu);
        return requirement.test(ExecuteContext.of(menu, slotContent), placeholders);
    }

    @Override
    public Requirement requirement() {
        return requirement;
    }

    public boolean isEmpty() {
        return requirement == null;
    }
}
