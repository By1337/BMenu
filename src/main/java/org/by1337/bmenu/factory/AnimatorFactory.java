package org.by1337.bmenu.factory;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.bmenu.MenuLoader;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.FrameOpcode;
import org.by1337.bmenu.animation.FrameOpcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnimatorFactory {
    private static final YamlCodec<List<Map<String, YamlValue>>> MAP_LIST = YamlCodec.mapOf(YamlCodec.STRING, YamlCodec.identity()).listOf();

    @Deprecated(forRemoval = true)
    public static Animator.AnimatorContext read(List<YamlContext> frames, MenuLoader loader) {
        throw new UnsupportedOperationException();
    }


}