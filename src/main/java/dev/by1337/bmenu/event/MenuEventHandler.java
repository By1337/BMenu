package dev.by1337.bmenu.event;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import org.jetbrains.annotations.NotNull;

public interface MenuEventHandler {
    YamlCodec<MenuEventHandler> CODEC = new YamlCodec<MenuEventHandler>() {
        @Override
        public DataResult<MenuEventHandler> decode(YamlValue yaml) {
            if (yaml.isMap()) return LegacyHandler.CODEC.decode(yaml)
                    .mapValue(v -> v);
            return Commands.CODEC.decode(yaml)
                    .mapValue(v -> v);
        }

        @Override
        public YamlValue encode(MenuEventHandler handler) {
            if (handler instanceof LegacyHandler l) return LegacyHandler.CODEC.encode(l);
            if (handler instanceof Commands c) return Commands.CODEC.encode(c);
            return YamlValue.wrap(handler.toString());
        }

        @Override
        public @NotNull SchemaType schema() {
            return Commands.CODEC.schema();
        }
    };

    void run(ExecuteContext ctx, PlaceholderApplier placeholders);
}
