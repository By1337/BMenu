package dev.by1337.bmenu.animation.opcode;

import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.animation.FrameOpcode;
import dev.by1337.bmenu.animation.FrameOpcodes;
import org.jetbrains.annotations.Nullable;

public class SetTitleOpcode implements FrameOpcode {
    public static final YamlCodec<SetTitleOpcode> CODEC = YamlCodec.STRING.schema(SchemaTypes.STRING_OR_NUMBER)
            .map(SetTitleOpcode::new, v -> v.title);
    private final String title;

    public SetTitleOpcode(String value) {
        title = value;
    }

    @Override
    public void apply(SlotContent[] matrix, Menu menu, Animator animator) {
        menu.setTitle(menu.setPlaceholders(title));
    }
    @Override
    public @Nullable FrameOpcodes type() {
        return FrameOpcodes.TITLE;
    }
}
