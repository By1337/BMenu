package org.by1337.bmenu.animation;

import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.animation.impl.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum FrameOpcodes {
    SET(SetAnimOpcode::new, "set", "st"),
    REMOVE(RemoveAnimOpcode::new, "remove", "rm"),
    MOVE(MoveAnimOpcode::new, "move", "mv"),
    SWAP(SwapAnimOpcode::new, "swap", "sw"),
    COPY(CopyAnimOpcode::new, "copy", "cp"),
    GOTO(GotoAnimOpcode::new, "goto", "gt"),
    SOUND(SoundAnimOpcode::new, "sound", "snd"),
    COMMANDS(CommandsAnimOpcode::new, "commands", "cmd"),
    TITLE(SetTitleOpcode::new, "title", "ttl"),
    FILL(FillAnimOpcode::new, "fill", "fl"),
    REMOVE_IF_NOT_EMPTY(RemoveIfNotEmptyAnimOpcode::new, "remove-if-not-empty", "rne"),
    ;
    private static final Map<String, FrameOpcodes> LOOKUP;
    private final Function<YamlValue, FrameOpcode> creator;
    private final String id;
    private final String[] aliases;

    FrameOpcodes(Function<YamlValue, FrameOpcode> creator, String id) {
        this.creator = creator;
        this.id = id;
        aliases = new String[]{};
    }

    FrameOpcodes(Function<YamlValue, FrameOpcode> creator, String id, String... aliases) {
        this.creator = creator;
        this.id = id;
        this.aliases = aliases;
    }

    public Function<YamlValue, FrameOpcode> getCreator() {
        return creator;
    }

    public String getId() {
        return id;
    }

    public String[] aliases() {
        return aliases;
    }

    public static FrameOpcodes byId(String id) {
        return LOOKUP.get(id);
    }

    static {
        LOOKUP = new HashMap<>();
        for (FrameOpcodes value : values()) {
            LOOKUP.put(value.id, value);
            for (String alias : value.aliases) {
                LOOKUP.put(alias, value);
            }
        }
    }
}
