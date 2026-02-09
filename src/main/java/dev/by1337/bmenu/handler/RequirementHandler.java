package dev.by1337.bmenu.handler;

import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RequirementHandler {
    YamlCodec<RequirementHandler> CODEC = new YamlCodec<>() {
        @Override
        public DataResult<RequirementHandler> decode(YamlValue yaml) {
            if (yaml.isMap()) return LegacyHandler.CODEC.decode(yaml).mapValue(v -> v);
            return RequirementHandlerImpl.CODEC.decode(yaml).mapValue(v -> v);
        }

        @Override
        public YamlValue encode(RequirementHandler h) {
            if (h instanceof LegacyHandler l) return LegacyHandler.CODEC.encode(l);
            if (h instanceof RequirementHandlerImpl v) return RequirementHandlerImpl.CODEC.encode(v);
            return YamlValue.wrap(h.toString());
        }

        @Override
        public @NotNull SchemaType schema() {
            return SchemaTypes.ANY;
        }
    };
    boolean test(Menu menu, PlaceholderApplier placeholders);

    record RequirementHandlerImpl(List<Requirement> requirements) implements RequirementHandler{
        public static final YamlCodec<RequirementHandlerImpl> CODEC = Requirement.CODEC.listOf()
                .map(RequirementHandlerImpl::new, RequirementHandlerImpl::requirements);

        @Override
        public boolean test(Menu menu, PlaceholderApplier placeholders) {
            for (Requirement requirement : requirements) {
                if (!requirement.test(menu, placeholders)) return false;
            }
            return true;
        }
    }
}
//