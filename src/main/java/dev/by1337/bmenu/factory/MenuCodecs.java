package dev.by1337.bmenu.factory;

import com.google.common.base.Joiner;
import dev.by1337.bmenu.animation.util.AnimationUtil;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MenuCodecs {

    public static final YamlCodec<Map<String, String>> ARGS_CODEC =
            YamlCodec.mapOf(
                    YamlCodec.STRING,
                    YamlCodec.MULTI_LINE_STRING.schema(SchemaTypes.anyOf(SchemaTypes.STRING_OR_NUMBER, SchemaTypes.STRING_OR_NUMBER.listOf()))
            );

    public static final YamlCodec<int[]> SLOTS_CODEC = YamlCodec.STRINGS.map(
            l -> AnimationUtil.readSlots(Joiner.on(",").join(l)),
            arr -> List.of(Arrays.toString(arr).replace("[", "").replace("]", ""))
    );
}

