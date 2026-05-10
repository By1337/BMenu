package dev.by1337.bmenu.loader;

import dev.by1337.bmenu.factory.MenuFilePostprocessor;
import dev.by1337.bmenu.factory.MenuFilePreprocessor;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MenuDecoder {

    public final Map<File, DataResult<? extends MenuConfig>> loaded = new HashMap<>();
    public final MenuLoader loader;
    private PlaceholderApplier placeholders = s -> s;

    public MenuDecoder(MenuLoader loader) {
        this.loader = loader;
    }

    public DataResult<? extends MenuConfig> decode(File file) {
        try {
            loadFile(file);

            DataResult<? extends MenuConfig> result = loaded.get(file);

            Map<File, MenuConfig> configs = new HashMap<>();
            for (var entry : loaded.entrySet()) {
                var v = entry.getValue();
                if (v.hasError()) {
                    result = result.withError(v.error());
                }
                if (v.hasResult()) {
                    configs.put(entry.getKey(), v.result());
                }
            }
            MenuConfig base = result.result();
            if (base == null) return result;
            base.setLoader(loader);
            base.postDecode(file, configs);
            return result;
        } catch (Exception e) {
            return DataResult.error(e.getMessage(), e);
        }
    }

    public YamlMap readYaml(File file) {
        return MenuFilePostprocessor.apply(placeholders.setPlaceholders(MenuFilePreprocessor.loadFile(file, loader)));
    }

    public @Nullable MenuConfig loadFile(File file) {
        var old = loaded.get(file);
        if (old != null) return old.result();
        return loadFile(file, readYaml(file));
    }

    public @Nullable MenuConfig loadFile(File file, YamlMap ctx) {
        var old = loaded.get(file);
        if (old != null) return old.result();
        DataResult<? extends MenuConfig> result = decode(ctx).mapErrorIfHas(s -> "In File: " + file + "\n" + s);
        loaded.put(file, result);
        var decoded = result.result();
        if (decoded != null) {
            decoded.setLoader(loader);
            decoded.onDecode(ctx, file, this);
        }
        return decoded;
    }

    public DataResult<? extends MenuConfig> decode(YamlMap ctx) {
        String provider = ctx.get("provider").asString("bmenu:default");
        YamlCodec<? extends MenuConfig> codec = loader.findMenuCodec(provider);
        if (codec == null) return DataResult.error("unknown provider " + provider);
        return codec.decode(ctx);
    }

    public void setPlaceholders(PlaceholderApplier placeholders) {
        this.placeholders = placeholders;
    }
}
