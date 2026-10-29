package dev.by1337.bmenu.requirement;

import dev.by1337.bmenu.command.PlayerContext;
import dev.by1337.bmenu.event.MenuEvents;
import dev.by1337.edsl.EventHandler;
import dev.by1337.edsl.check.CheckHandler;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.YamlDecoder;
import org.jetbrains.annotations.Nullable;

/** Compatibility interface. All configuration checks are decoded and executed by EventDSL. */
@FunctionalInterface
public interface Requirement {
    YamlDecoder<Requirement> DECODER = MenuEvents.REQUIREMENT_DECODER;
    @Deprecated
    YamlCodec<Requirement> CODEC = YamlCodec.of(DECODER);
    Requirement TRUE = of(CheckHandler.nopTrue(), YamlValue.wrap("true"));
    Requirement FALSE = of(CheckHandler.nopFalse(), YamlValue.wrap("false"));

    boolean test(PlayerContext ctx, PlaceholderApplier placeholders);

    default @Nullable Requirement compile() {
        return null;
    }

    default YamlValue encode() {
        return YamlValue.wrap(toString());
    }

    static Requirement of(EventHandler handler) {
        return of(handler, YamlValue.wrap("event handler"));
    }

    static Requirement of(EventHandler handler, YamlValue source) {
        return new Requirement() {
            @Override
            public boolean test(PlayerContext ctx, PlaceholderApplier placeholders) {
                return MenuEvents.test(handler, ctx, placeholders);
            }

            @Override
            public YamlValue encode() {
                return source;
            }
        };
    }
}
