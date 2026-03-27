package dev.by1337.bmenu.factory;

import com.google.common.base.Joiner;
import dev.by1337.bmenu.animation.util.AnimationUtil;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

    public static final YamlCodec<String> LOWER_CASE_STRING = YamlCodec.STRING.map(
            String::toLowerCase,
            Function.identity()
    );
    public static final YamlCodec<NamespacedKey> NAMESPACED_KEY = LOWER_CASE_STRING.flatMap((s) -> {
        try {
            if (s.contains(":")) {
                NamespacedKey key = NamespacedKey.fromString(s);
                return key == null ? DataResult.error("Failed to decode NamespacedKey: Expected: '<space>:<name>', but got " + s) : DataResult.success(key);
            } else {
                return DataResult.success(NamespacedKey.minecraft(s));
            }
        } catch (Exception e) {
            return DataResult.error("Failed to decode NamespacedKey: " + e.getMessage());
        }
    }, NamespacedKey::asString);

    public static YamlCodec<List<String>> STR_LIST = new YamlCodec<>() {

        @Override
        public DataResult<List<String>> decode(YamlValue yaml) {
            if (!yaml.isCollection()) return STRING.decode(yaml).mapValue(s ->
                    Arrays.asList(s.replace("\\n", "\n").split("\n"))
            );
            return yaml.asCollection().flatMap(o -> {
                StringBuilder err = new StringBuilder();
                List<String> result = new ArrayList<>();
                for (Object object : o) {
                    var res = decode(YamlValue.wrap(object));
                    if (res.hasResult()){
                        result.addAll(res.result());
                    }
                    if (res.hasError()){
                        err.append(res.error()).append("\n");
                    }
                }
                if (err.isEmpty()) return DataResult.success(result);
                err.setLength(err.length() - 1);
                return DataResult.error(err.toString()).partial(result);
            });
        }

        @Override
        public YamlValue encode(List<String> strings) {
            return YamlValue.wrap(strings);
        }

    };
    public static YamlCodec<String> MULTI_LINE_STRING = STR_LIST.map(
            l -> Joiner.on("\n").join(l),
            s -> Arrays.asList(s.replace("\\n", "\n").split("\n"))
    );

}
