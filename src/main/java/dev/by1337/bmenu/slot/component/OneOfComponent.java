package dev.by1337.bmenu.slot.component;

import dev.by1337.bmenu.slot.SlotVariant;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OneOfComponent {
    public static final YamlCodec<OneOfComponent> CODEC = new YamlCodec<OneOfComponent>() {
        static final YamlCodec<Map<String, SlotVariant>> MAP_CODEC = YamlCodec.mapOf(YamlCodec.STRING,
                SlotVariant.CODEC);
        static final YamlCodec<List<SlotVariant>> LIST_CODEC = SlotVariant.CODEC.listOf();
        static final SchemaType SCHEMA_TYPE = MAP_CODEC.schema().or(LIST_CODEC.schema());

        @Override
        public DataResult<OneOfComponent> decode(YamlValue yaml) {
            if (!yaml.isList()){
                return MAP_CODEC.decode(yaml).flatMap(map -> {
                    Entry[] entries = new Entry[map.size()];
                    int x = 0;
                    for (Map.Entry<String, SlotVariant> entry : map.entrySet()) {
                        entries[x] = new Entry(entry.getValue(), x, entry.getKey());
                        x++;
                    }
                    return DataResult.success(new OneOfComponent(entries));
                });
            }
            return LIST_CODEC.decode(yaml).flatMap(list -> {
                Entry[] entries = new Entry[list.size()];
                int x = 0;
                for (SlotVariant variant : list) {
                    entries[x] = new Entry(variant, x, "$" + x);
                    x++;
                }
                return DataResult.success(new OneOfComponent(entries));
            });
        }

        @Override
        public YamlValue encode(OneOfComponent src) {
            Map<String, SlotVariant> map = new LinkedHashMap<>();
            for (Entry entry : src.entries) {
                map.put(entry.name, entry.variant);
            }
            return MAP_CODEC.encode(map);
        }

        @Override
        public @NotNull SchemaType schema() {
            return SCHEMA_TYPE;
        }
    };
    private final Entry[] entries;
    private final Object2ObjectOpenHashMap<String, Entry> byName;
    private final boolean ticking;

    public OneOfComponent(Entry[] entries) {
        this.entries = entries;
        byName = new Object2ObjectOpenHashMap<>();
        boolean ticking = false;
        for (Entry entry : entries) {
            byName.put(entry.name, entry);
            if (entry.variant.ticker() != null){
                ticking = true;
            }
        }
        this.ticking = ticking;
    }

    public int getSize() {
        return entries.length;
    }

    public Entry byName(String name) {
        return byName.get(name);
    }

    public Entry[] entries() {
        return entries;
    }

    public boolean isTicking() {
        return ticking;
    }

    public record Entry(SlotVariant variant, int index, String name) {
    }
}
