package org.by1337.bmenu.factory;

import dev.by1337.yaml.BukkitYamlCodecs;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.YamlField;
import dev.by1337.yaml.codec.schema.JsonSchemaTypeBuilder;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.bukkit.event.inventory.InventoryType;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.MenuConfig;
import org.by1337.bmenu.MenuItemBuilder;
import org.by1337.bmenu.MenuLoader;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.command.CommandList;
import org.by1337.bmenu.requirement.CommandRequirements;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class MenuCodec {
    private static final List<YamlField<MenuCodec, ?>> FIELDS;
    private static final SchemaType SCHEMA_TYPE;

    private final File file;
    private final MenuLoader loader;

    private final MenuLoadContext ctx;
    private List<MenuConfig> supers = new ArrayList<>();
    private String title;
    private @Nullable SpacedNameKey id;
    private @Nullable SpacedNameKey provider;
    private InventoryType type;
    private int size;
    private List<SpacedNameKey> onlyOpenFrom = new ArrayList<>();
    private Map<String, String> args = new HashMap<>();
    private Map<String, MenuItemBuilder> items = new HashMap<>();
    private Animator.AnimatorContext animator;
    private Map<String, Animator.AnimatorContext> animations = new HashMap<>();
    private CommandList commandList = new CommandList(new HashMap<>());
    private Map<String, CommandRequirements> menuEventListeners = new HashMap<>();
    private final YamlCodec<List<MenuConfig>> other_configs_loader = new YamlCodec<List<MenuConfig>>() {
        @Override
        public DataResult<List<MenuConfig>> decode(YamlValue yamlValue) {
            return YamlCodec.STRINGS.decode(yamlValue).flatMap(v -> load(FileUtil.findFiles(file, loader, v)));
        }

        @Override
        public YamlValue encode(List<MenuConfig> menuConfigs) {
            return YamlValue.wrap(menuConfigs.stream().filter(c -> c.getId() != null).map(c -> c.getId().toString()).toList());
        }

        @Override
        public @NotNull SchemaType schema() {
            return YamlCodec.STRINGS.schema();
        }
    };
    private final List<YamlField<MenuCodec, ?>> runtimeCodecs = new ArrayList<>();

    public MenuCodec(File file, MenuLoader loader) {
        this(file, loader, new MenuLoadContext());
    }

    private MenuCodec(File file, MenuLoader loader, MenuLoadContext ctx) {
        this.file = file;
        this.loader = loader;
        this.ctx = ctx;
        runtimeCodecs.add(new YamlField<>(other_configs_loader, "extends", m -> m.supers, (m, v) -> m.supers = v, List.of()));
    }


    public DataResult<MenuConfig> decode() {
        this.ctx.loadedFiles.add(file);
        YamlMap ctx = MenuFilePostprocessor.apply(MenuFilePreprocessor.loadFile(file, loader));
        StringBuilder error = new StringBuilder();
        doDecode(FIELDS, error, ctx);
        doDecode(runtimeCodecs, error, ctx);

        var result = new MenuConfig(
                supers,
                id,
                provider,
                type,
                size,
                onlyOpenFrom,
                args,
                items,
                ctx,
                loader,
                title,
                animator,
                commandList,
                menuEventListeners,
                animations,
                this.ctx.loadedFiles
        );
        if (!error.isEmpty()) {
            error.setLength(error.length() - 1);
            return DataResult.error(error.toString()).partial(result);
        }
        return DataResult.success(result);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void doDecode(List<YamlField<MenuCodec, ?>> codecs, StringBuilder error, YamlMap ctx) {
        for (YamlField field : codecs) {
            if (ctx.has(field.name())) {
                DataResult<?> result = field.codec().decode(ctx.get(field.name()));
                if (result.hasError()) {
                    error.append("Failed to decode field '").append(field.name()).append("':\n  - ").append(result.error().replace("\n", "\n    ")).append("\n");
                }
                if (result.hasResult()) {
                    field.setter().accept(this, result.result());
                } else if (field.defaultValue() != null) {
                    field.setter().accept(this, field.defaultValue());
                }
            } else if (field.defaultValue() != null) {
                field.setter().accept(this, field.defaultValue());
            }
        }
    }

    public static @NotNull SchemaType schema() {
        return SCHEMA_TYPE;
    }

    static {
        FIELDS = new ArrayList<>();
//        FIELDS.add(new YamlField<MenuCodec, List<String>>(YamlCodec.STRINGS, "extends",
//                m -> m.supers.stream().filter(c -> c.getId() != null).map(c -> c.getId().toString()).toList(),
//                (m, v) -> m.supers = m.load(FileUtil.findFiles(m.file, m.loader, v))
//        ));
//        FIELDS.add(new YamlField<MenuCodec, List<String>>(YamlCodec.STRINGS, "extends",
//                m -> m.supers.stream().filter(c -> c.getId() != null).map(c -> c.getId().toString()).toList(),
//                (m, v) -> m.supers = m.load(FileUtil.findFiles(m.file, m.loader, v))
//        ));
        FIELDS.add(YamlCodec.STRING.fieldOf("title", m -> m.title, (m, v) -> m.title = v, "&7Title!"));
        FIELDS.add(YamlCodec.STRING.fieldOf("id", m -> m.id == null ? null : m.id.toString(), (m, v) -> m.id = m.asId(v)));
        FIELDS.add(YamlCodec.STRING.fieldOf("provider", m -> m.provider == null ? null : m.provider.toString(), (m, v) -> m.provider = m.asId(v)));
        FIELDS.add(BukkitYamlCodecs.INVENTORY_TYPE.fieldOf("type", m -> m.type, (m, v) -> m.type = v, InventoryType.CHEST));
        FIELDS.add(YamlCodec.INT.fieldOf("size", m -> m.size, (m, v) -> m.size = v, 54));
        FIELDS.add(YamlCodec.STRINGS.fieldOf("only-open-from",
                m -> m.onlyOpenFrom.stream().map(SpacedNameKey::toString).toList(),
                (m, v) -> m.onlyOpenFrom = v.stream().map(m::asId).toList(),
                List.of()
        ));
        FIELDS.add(MenuCodecs.ARGS_CODEC.fieldOf("args", m -> m.args, (m, v) -> m.args = v, Map.of()));
        FIELDS.add(YamlCodec.mapOf(YamlCodec.STRING, MenuItemBuilder.YAML_CODEC).fieldOf("items", m -> m.items, (m, v) -> m.items = v, Map.of()));
        FIELDS.add(Animator.AnimatorContext.CODEC.fieldOf("animation", m -> m.animator, (m, v) -> m.animator = v));
        FIELDS.add(YamlCodec.mapOf(YamlCodec.STRING, Animator.AnimatorContext.CODEC).fieldOf("animations", m -> m.animations, (m, v) -> m.animations = v));
        FIELDS.add(CommandList.CODEC.fieldOf("commands-list", m -> m.commandList, (m, v) -> m.commandList = v));
        FIELDS.add(YamlCodec.mapOf(YamlCodec.STRING, CommandRequirements.CODEC).fieldOf("menu-events", m -> m.menuEventListeners, (m, v) -> m.menuEventListeners = v));


        var builder = JsonSchemaTypeBuilder
                .create()
                .type(SchemaTypes.Type.OBJECT);

        for (YamlField<MenuCodec, ?> field : FIELDS) {
            builder.properties(field.name(), field.codec().schema());
        }
        builder.patternProperties("^items-", MenuItemBuilder.YAML_CODEC.schema().asMap());
        builder.definitions(MenuCodecs.COMMANDS_SCHEMA_TYPE_REF_NAME, MenuCodecs.COMMANDS_SCHEMA_TYPE);
        SCHEMA_TYPE = builder.build();
    }

    private SpacedNameKey asId(@Nullable String id) {
        if (id == null) return null;
        if (id.contains(":")) {
            return new SpacedNameKey(id);
        } else {
            return new SpacedNameKey(loader.getPlugin().getName().toLowerCase(Locale.ROOT), id);
        }
    }

    private DataResult<List<MenuConfig>> load(List<File> files) {
        if (files.isEmpty()) return DataResult.success(Collections.emptyList());
        List<MenuConfig> result = new ArrayList<>();
        StringBuilder error = new StringBuilder();
        for (File file : files) {
            if (ctx.loadedFiles.contains(file)) continue;
            MenuCodec menuCodec = new MenuCodec(file, loader, ctx);
            var v = menuCodec.decode();
            if (v.hasError()) {
                error.append("Failed to load file ").append(file.getPath()).append("\n").append(v.error()).append("\n");
            }
            if (v.hasResult()){
                result.add(v.result());
            }
        }
        if (!error.isEmpty()) {
            error.setLength(error.length() - 1);
            return DataResult.error(error.toString()).partial(result);
        }
        return DataResult.success(result);
    }

    private static class MenuLoadContext {
        private final List<File> loadedFiles = new ArrayList<>();
    }
}
