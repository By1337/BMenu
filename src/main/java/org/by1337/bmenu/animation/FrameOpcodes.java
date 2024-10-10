package org.by1337.bmenu.animation;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.animation.impl.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum FrameOpcodes {
    SET(SetAnimOpcode::new, "set"),
    REMOVE(RemoveAnimOpcode::new, "remove"),
    MOVE(MoveAnimOpcode::new, "move"),
    SWAP(SwapAnimOpcode::new, "swap"),
    COPY(CopyAnimOpcode::new, "copy"),
    GOTO(GotoAnimOpcode::new, "goto"),
    SOUND(SoundAnimOpcode::new, "sound"),
    COMMANDS(CommandsAnimOpcode::new, "commands"),
    ;
    private static final Map<String, FrameOpcodes> LOOKUP;
    private final Function<YamlValue, FrameOpcode> creator;
    private final String id;

    FrameOpcodes(Function<YamlValue, FrameOpcode> creator, String id) {
        this.creator = creator;
        this.id = id;
    }

    public Function<YamlValue, FrameOpcode> getCreator() {
        return creator;
    }

    public String getId() {
        return id;
    }

    public static FrameOpcodes byId(String id) {
        return LOOKUP.get(id);
    }

    static {
        LOOKUP = new HashMap<>();
        for (FrameOpcodes value : values()) {
            LOOKUP.put(value.getId(), value);
        }
    }
}
