package org.by1337.bmenu.factory;

import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.YamlValue;
import org.by1337.bmenu.MenuLoader;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.FrameOpcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnimatorFactory {

    public static Animator.AnimatorContext read(List<YamlContext> frames, MenuLoader loader) {
        List<Animator.Frame> frameList = new ArrayList<Animator.Frame>();
        int lastPos = 0;
        for (YamlContext frame : frames) {
            List<Map<String, YamlValue>> opcodesRaw = frame.get("opcodes").getAsList(v -> v.getAsMap(YamlValue::getAsString, v1 -> v1));
            List<FrameOpcode> opcodes = new ArrayList<>();
            for (Map<String, YamlValue> map : opcodesRaw) {
                for (String string : map.keySet()) {
                    FrameOpcodes type = FrameOpcodes.byId(string);
                    if (type == null) {
                        loader.getLogger().error("Unknown opcode type: {}", string);
                        continue;
                    }
                    try {
                        opcodes.add(type.getCreator().apply(map.get(string)));
                    } catch (Throwable e) {
                        loader.getLogger().error("Failed to load opcode {}: {}", string, map.get(string).getAsObject(), e);
                    }
                }
            }
            int pos = frame.getAsInteger("tick", ++lastPos);
            lastPos = pos;
            frameList.add(new Animator.Frame(pos, opcodes));
        }
        return new Animator.AnimatorContext(frameList);
    }
}
