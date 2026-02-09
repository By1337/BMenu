package dev.by1337.bmenu.slot.component;

import dev.by1337.bmenu.handler.MenuEventHandler;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.Menu;

import java.util.List;

public record OnTickComponent(MenuEventHandler handler) {
    public static final OnTickComponent DEFAULT = new OnTickComponent(new Commands(List.of("[rebuild]")));
    public static final YamlCodec<OnTickComponent> CODEC = MenuEventHandler.CODEC.map(
            OnTickComponent::new,
            t -> t.handler
    );


    public void tick(SlotContent slotContent, Menu menu) {
        var placeholder = menu.resolvers().and(slotContent).bind(menu);
        ExecuteContext ctx = ExecuteContext.of(menu, slotContent);
        handler.run(ctx, placeholder);
    }

    public boolean shouldTick(int ticks, int tickSpeed) {
        return ticks % tickSpeed == 0;
    }
}
