package dev.by1337.bmenu.menu.select;

import dev.by1337.bmenu.command.PlayerContext;
import dev.by1337.bmenu.factory.FileUtil;
import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.handler.trace.EventTracer;
import dev.by1337.bmenu.loader.MenuConfig;
import dev.by1337.bmenu.loader.MenuDecoder;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.bmenu.util.math.FastExpressionParser;
import dev.by1337.plc.PapiResolver;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.Placeholders;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.IntFunction;

public class SelectMenuConfig extends MenuConfig {
    private static final Logger log = LoggerFactory.getLogger("BMenu#Select");
    private static final Random rand = new Random();
    private static final DecimalFormat df = new DecimalFormat("#.##");
    private static final PlaceholderResolver<Player> PLACEHOLDERS = Placeholders.<Player>create()
            .of("rand_bool", rand::nextBoolean)
            .withParams("rand", v -> tryToInt(v, rand::nextInt))
            .withParams("math", in -> {
                try {
                    return df.format(FastExpressionParser.parse(in));
                } catch (FastExpressionParser.MathFormatException e) {
                    log.error(e.getMessage(), e);

                }
                return null;
            })
            .and(PapiResolver.INSTANCE.map(v -> v));
    public static final YamlCodec<SelectMenuConfig> CODEC = PipelineYamlCodecBuilder.of(SelectMenuConfig::new)
            .and(MenuConfig.RAW_CODEC)
            .field(MenuSelector.CODEC, "selector", v -> v.selector, (m, v) -> m.selector = v)
            .build();

    private final Map<String, MenuConfig> menus = new HashMap<>();
    private MenuSelector selector;

    @Override
    public Menu create(Player viewer, @Nullable Menu previousMenu) {
        if (selector == null) throw new IllegalStateException("has no selector!");
        var res = selector.select(new PlayerContext() {
            @Override
            public Player getPlayer() {
                return viewer;
            }

            @Override
            public EventTracer tracer() {
                return EventTracer.NOP;
            }
        }, PLACEHOLDERS.bind(viewer));
        if (res == null) {
            throw new IllegalStateException("Has no select menu! " + id);
        }
        var v = menus.get(res);
        if (v == null) {
            throw new IllegalStateException("Unknown menu " + res);
        }
        return v.create(viewer, previousMenu);
    }

    @Override
    public void onDecode(YamlMap from, File fromFile, MenuDecoder decoder) {
        Map<String, FileConf> menus = from.get("menus").decode(YamlCodec.mapOf(YamlCodec.STRING, FileConf.CODEC)).result();
        if (menus == null) return;
        for (Map.Entry<String, FileConf> entry : menus.entrySet()) {
            FileConf conf = entry.getValue();
            var path = conf.file;
            var file = FileUtil.findFile(fromFile, decoder.loader, path);
            var menuDecoder = new MenuDecoder(decoder.loader);

            var replaces = conf.replaces;
            if (!replaces.isEmpty()) {
                Placeholders<Void> plc = new Placeholders<>();
                replaces.forEach(plc::of);
                menuDecoder.setPlaceholders(plc.bind(null));
            }
            var decoded = menuDecoder.decode(file).getOrThrow();
            decoded.setId(id);
            this.menus.put(entry.getKey(), decoded);
        }
    }

    @Override
    public void postDecode(File base, Map<File, MenuConfig> configs) {
        supers(List.of(), new ArrayList<>(configs.keySet()));
    }

    private static class FileConf {
        public static final YamlCodec<FileConf> CODEC = RecordYamlCodecBuilder.mapOf(
                FileConf::new,
                YamlCodec.STRING.fieldOf("file", v -> v.file),
                MenuCodecs.ARGS_CODEC.fieldOf("replaces", v -> v.replaces, Map.of())
        ).whenPrimitive(YamlCodec.STRING.map(s -> new FileConf(s, Map.of()), v -> v.file));
        private final String file;
        private final Map<String, String> replaces;

        private FileConf(String file, Map<String, String> replaces) {
            this.file = file;
            this.replaces = replaces;
        }
    }

    public interface MenuSelector {
        YamlCodec<MenuSelector> CODEC = YamlCodec.recursive(self -> new YamlCodec<MenuSelector>() {
            private final YamlCodec<Map<Requirement, MenuSelector>> map = YamlCodec.mapOf(Requirement.CODEC, self);

            @Override
            public DataResult<MenuSelector> decode(YamlValue yaml) {
                if (yaml.isPrimitive()) {
                    return yaml.decode(STRING)
                            .map(s -> {
                                var s1 = s.replace("#", ".");
                                return (v, v1) -> s1;
                            });
                }
                return yaml.decode(map).map(m -> (ctx, plc) -> {
                    for (var entry : m.entrySet()) {
                        if (entry.getKey().test(ctx, plc)) {
                            return entry.getValue().select(ctx, plc);
                        }
                    }
                    return null;
                });
            }

            @Override
            public YamlValue encode(MenuSelector menuSelector) {
                return YamlValue.wrap("no impl encoder");
            }
        });

        String select(PlayerContext ctx, PlaceholderApplier placeholders);
    }

    private static <R> @Nullable R tryToInt(String in, IntFunction<R> func) {
        try {
            double d = FastExpressionParser.parse(in);
            return func.apply((int) d);
        } catch (FastExpressionParser.MathFormatException e) {
            return null;
        }
    }
}