package dev.by1337.bmenu.loader;

import dev.by1337.bmenu.factory.FileUtil;
import dev.by1337.bmenu.factory.MenuFilePostprocessor;
import dev.by1337.bmenu.factory.MenuFilePreprocessor;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;

import java.io.File;
import java.util.*;

public class MenuConfigDecoder {
    private final MenuCodecRegistry codecRegistry;
    private final File file;
    private final MenuLoader loader;
    private final MenuLoadContext ctx;


    public MenuConfigDecoder(File file, MenuLoader loader, MenuCodecRegistry codecRegistry) {
        this(codecRegistry, file, loader, new MenuLoadContext());
    }

    private MenuConfigDecoder(MenuCodecRegistry codecRegistry, File file, MenuLoader loader, MenuLoadContext ctx) {
        this.codecRegistry = codecRegistry;
        this.file = file;
        this.loader = loader;
        this.ctx = ctx;

    }

    public DataResult<? extends MenuConfig> decode() {
        Map<File, DataResult<? extends MenuConfig>> loaded = new HashMap<>();
        Map<MenuConfig, List<File>> cfgToSupers = new IdentityHashMap<>();
        recursiveLoad(file, loaded, cfgToSupers);


        Map<File, MenuConfig> configs = new HashMap<>();
        StringBuilder errors = new StringBuilder();
        loaded.forEach((k, v) -> {
            if (v.hasError()) {
                errors.append(v.error()).append("\n");
            }
            if (v.hasResult()) {
                configs.put(k, v.result());
            }
        });

        DataResult<? extends MenuConfig> result = loaded.get(file);
        if (!result.hasResult()) return DataResult.error(errors.toString());
        MenuConfig base = result.result();
        List<File> fromFiles = new ArrayList<>(loaded.keySet());
        base.setLoader(loader);
        configs.remove(file);
        base.supers(new ArrayList<>(configs.values()), fromFiles);
        if (errors.isEmpty()) return DataResult.success(base);
        return DataResult.error(errors.toString()).partial(base);
    }


    private void recursiveLoad(File file, Map<File, DataResult<? extends MenuConfig>> loaded, Map<MenuConfig, List<File>> cfgToSupers) {
        if (loaded.containsKey(file)) return;
        YamlMap ctx = MenuFilePostprocessor.apply(MenuFilePreprocessor.loadFile(file, loader));
        var decoded = decode(ctx).mapErrorIfHas(s -> "In File: " + file + "\n" + s);
        loaded.put(file, decode(ctx));
        if (decoded.hasResult()) {
            List<File> supers = FileUtil.findFiles(file, loader, ctx.get("extends").asList(YamlCodec.STRING, List.of()));
            cfgToSupers.put(decoded.result(), supers);
            for (File aSuper : supers) {
                recursiveLoad(aSuper, loaded, cfgToSupers);
            }
        }

    }

    public DataResult<? extends MenuConfig> decode(YamlMap ctx) {
        String provider = ctx.get("provider").asString("bmenu:default");
        YamlCodec<? extends MenuConfig> codec = codecRegistry.get(provider);
        if (codec == null) return DataResult.error("unknown provider " + provider);
        return codec.decode(ctx);
    }

    private static class MenuLoadContext {
        private final List<File> loadedFiles = new ArrayList<>();
    }
}
