package dev.by1337.bmenu.menu.select;

import dev.by1337.bmenu.command.PlayerContext;
import dev.by1337.bmenu.factory.FileUtil;
import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.handler.trace.EventTracer;
import dev.by1337.bmenu.loader.MenuConfig;
import dev.by1337.bmenu.loader.v2.MenuDecoder;
import dev.by1337.bmenu.loader.v2.MenuDecoders;
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
import dev.by1337.yaml.decoder.RecordYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;
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
    public static final YamlDecoder<SelectMenuConfig> DECODER = PipelineYamlCodecBuilder.of(SelectMenuConfig::new)
            .and(MenuConfig.RAW_CODEC)
            .field(MenuSelector.DECODER, "selector", (m, v) -> m.selector = v)
            .buildDecoder();
    @Deprecated
    public static final YamlCodec<SelectMenuConfig> CODEC = YamlCodec.of(DECODER);

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
        Map<String, FileConf> menus = from.get("menus").decode(YamlCodec.of(YamlDecoder.mapOf(YamlDecoder.STRING, FileConf.DECODER))).result();
        if (menus == null) return;
        for (Map.Entry<String, FileConf> entry : menus.entrySet()) {
            FileConf conf = entry.getValue();
            var path = conf.file;
            var file = FileUtil.findFile(fromFile, decoder.loader(), path);
            var menuDecoder = MenuDecoders.factory().apply(decoder.loader());

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
        public static final YamlDecoder<FileConf> DECODER = RecordYamlDecoder.mapOf(
                FileConf::new,
                YamlDecoder.STRING.fieldOf("file"),
                MenuCodecs.ARGS_CODEC.asDecoder().fieldOf("replaces", Map.of())
        ).whenPrimitive(YamlDecoder.STRING.map(s -> new FileConf(s, Map.of())));
        private final String file;
        private final Map<String, String> replaces;

        private FileConf(String file, Map<String, String> replaces) {
            this.file = file;
            this.replaces = replaces;
        }
    }

    public interface MenuSelector {
        YamlDecoder<MenuSelector> DECODER = YamlDecoder.recursive(self -> (ctx, yaml) -> {
            if (yaml.isPrimitive()) {
                return YamlDecoder.STRING.decode(ctx, yaml).map(value -> {
                    String id = value.replace("#", ".");
                    return (player, placeholders) -> id;
                });
            }
            return YamlDecoder.mapOf(dev.by1337.bmenu.event.MenuEvents.REQUIREMENT_DECODER, self)
                    .decode(ctx, yaml).map(map -> (player, placeholders) -> {
                        for (var entry : map.entrySet()) {
                            if (entry.getKey().test(player, placeholders)) return entry.getValue().select(player, placeholders);
                        }
                        return null;
                    });
        });
        @Deprecated
        YamlCodec<MenuSelector> CODEC = YamlCodec.of(DECODER);

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
