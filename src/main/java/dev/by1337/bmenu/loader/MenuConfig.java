package dev.by1337.bmenu.loader;

import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.command.CommandList;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.menu.DefaultMenu;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.SlotBuilderSource;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.bmenu.slot.SlotFactory;
import dev.by1337.bmenu.yaml.CashedYamlMap;
import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MenuConfig implements SlotBuilderSource, Keyed {
    private static final int DEFAULT_CLICK_COOLDOWN = Integer.getInteger("bmenu.click.cooldown", 100);
    public static final PipelineYamlCodecBuilder<MenuConfig> RAW_CODEC;
    public static final YamlCodec<MenuConfig> CODEC;

    private MenuSupplier defaultMenuCreator = DefaultMenu::new;
    private final Set<NamespacedKey> supersId = new HashSet<>();
    private final List<MenuConfig> supers = new ArrayList<>();
    private @Nullable NamespacedKey id;
    private @Nullable NamespacedKey provider = new NamespacedKey("bmenu", "default");
    private InventoryType invType = InventoryType.CHEST;
    private int size = 54;
    private final Set<NamespacedKey> onlyOpenFrom = new HashSet<>();
    private final Map<String, String> args = new HashMap<>();
    private final Map<String, SlotFactory> idToItems = new HashMap<>();
    private YamlMap yaml;
    private CashedYamlMap cachedYaml;
    private String title;
    private List<SlotFactory> items;
    private @Nullable Animator.AnimatorContext animation;
    private final Map<String, Animator.AnimatorContext> animations = new HashMap<>();
    private CommandList commandList = new CommandList(new HashMap<>());
    private final Map<String, Commands> eventHandlers = new HashMap<>();
    private long clickCooldown = DEFAULT_CLICK_COOLDOWN;

    private Object data;
    private MenuLoader loader;

    private List<File> fromFiles;

    public void fill(Menu menu, SlotContent[] matrix) {
        int invSize = matrix.length;

        for (SlotFactory builder : items) {
            SlotContent slotContent = builder.buildIfVisible(menu);
            if (slotContent == null) continue;
            var slots = builder.slots();
            for (int slot : slots) {
                if (slot < 0 || slot >= invSize) continue;
                matrix[slot] = slotContent;
            }
        }
    }

    public static YamlCodec<MenuConfig> createCodecFor(MenuSupplier t) {
        return MenuConfig.CODEC.map(
                v -> {
                    v.setDefaultMenuCreator(t);
                    return v;
                },
                v -> v
        );
    }

    public Menu create(Player viewer, @Nullable Menu previousMenu) {
        return defaultMenuCreator.createMenu(this, viewer, previousMenu);
    }

    public void supers(List<MenuConfig> supers, List<File> fromFiles) {
        this.fromFiles = fromFiles;
        Map<String, SlotFactory> items = new HashMap<>();
        for (int i = supers.size() - 1; i >= 0; i--) {
            MenuConfig superMenu = supers.get(i);
            items.putAll(superMenu.idToItems);
            superMenu.eventHandlers.forEach((k, v) -> {
                var old = eventHandlers.get(k);
                if (old == null) eventHandlers.put(k, v);
                else {
                    old.addHandler(v);
                }
            });
            if (superMenu.id != null) {
                supersId.add(superMenu.id);
            }
            this.onlyOpenFrom.addAll(superMenu.onlyOpenFrom);
            this.commandList.merge(superMenu.commandList);
            if (superMenu.animation != null) {
                if (this.animation == null) {
                    this.animation = superMenu.animation;
                } else {
                    this.animation.merge(superMenu.animation);
                }
            }
            for (String s : superMenu.animations.keySet()) {
                if (this.animations.containsKey(s)) {
                    this.animations.get(s).merge(superMenu.animations.get(s));
                } else {
                    this.animations.put(s, superMenu.animations.get(s));
                }
            }
        }
        items.putAll(idToItems);
        idToItems.clear();
        idToItems.putAll(items);
        this.items = idToItems.values().stream()
                .filter(SlotFactory::hasSlot)
                .sorted().toList();
    }

    public boolean canOpenFrom(@Nullable Menu menu) {
        if (onlyOpenFrom.isEmpty()) return true;
        if (menu == null || menu.config().id == null) return false;
        if (onlyOpenFrom.contains(menu.config().id)) return true;
        for (NamespacedKey spacedNameKey : menu.config().supersId) {
            if (supersId.contains(spacedNameKey)) return true;
        }
        return false;
    }

    public void setLoader(MenuLoader loader) {
        this.loader = loader;
    }

    public MenuSupplier defaultMenuCreator() {
        return defaultMenuCreator;
    }

    public void setDefaultMenuCreator(MenuSupplier defaultMenuCreator) {
        this.defaultMenuCreator = defaultMenuCreator;
    }

    static {
        RAW_CODEC = PipelineYamlCodecBuilder.of(MenuConfig::new)
                .string("title", m -> m.title, (m, v) -> m.title = v)
                .field(MenuCodecs.NAMESPACED_KEY, "id", m -> m.id, (m, v) -> m.id = v)
                .field(MenuCodecs.NAMESPACED_KEY, "provider", m -> m.provider, (m, v) -> m.provider = v)
                .field(BukkitCodecs.inventory_type(), "type", m -> m.invType, (m, v) -> m.invType = v)
                .integer("size", m -> m.size, (m, v) -> m.size = v)
                .field(MenuCodecs.NAMESPACED_KEY.listOf().asSet(), "only-open-from", m -> m.onlyOpenFrom, (m, v) -> m.onlyOpenFrom.addAll(v))
                .field(MenuCodecs.ARGS_CODEC, "args", m -> m.args, (m, v) -> m.args.putAll(v))
                .field(YamlCodec.mapOf(YamlCodec.STRING, SlotFactory.CODEC), "items", m -> m.idToItems, (m, v) -> m.idToItems.putAll(v))
                .field(Animator.AnimatorContext.CODEC, "animation", m -> m.animation, (m, v) -> m.animation = v)
                .field(YamlCodec.mapOf(YamlCodec.STRING, Animator.AnimatorContext.CODEC), "animations", m -> m.animations, (m, v) -> m.animations.putAll(v))
                .field(CommandList.CODEC, "commands-list", m -> m.commandList, (m, v) -> m.commandList = v)
                .field(YamlCodec.mapOf(YamlCodec.STRING, Commands.CODEC), "menu-events", m -> m.eventHandlers, (m, v) -> m.eventHandlers.putAll(v))
                .field(YamlCodec.LONG, "click_cooldown", m -> m.clickCooldown, (m, v) -> m.clickCooldown = v)
                .field(YamlCodec.YAML_MAP, null, m -> null, (m, v) -> {
                    m.yaml = v;
                    m.cachedYaml = new CashedYamlMap(v);
                })
        ;
        CODEC = RAW_CODEC.build();
    }

    public YamlCodec<? extends MenuConfig> codec() {
        return CODEC;
    }

    public void dump(Path toFolder) {
        dump(toFolder, getSaveName());
    }

    public void dump(Path toFolder, String name) {
        try {
            if (!toFolder.toFile().exists()) {
                toFolder.toFile().mkdirs();
            }

            // noinspection all
            Files.writeString(toFolder.resolve(name + ".yml"), ((YamlCodec) codec()).encode(this).asYamlMap().result().saveToString());

            int x = 0;
            for (MenuConfig superMenu : supers) {
                superMenu.dump(toFolder, name + "$" + superMenu.getSaveName() + "$" + x++);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSaveName() {
        return id == null ? "anonymous" : id.getNamespace() + "_" + id.getKey();
    }

    public Set<NamespacedKey> supersId() {
        return supersId;
    }

    public List<MenuConfig> supers() {
        return supers;
    }

    public @Nullable NamespacedKey id() {
        return id;
    }

    public @Nullable NamespacedKey provider() {
        return provider;
    }

    public InventoryType invType() {
        return invType;
    }

    public int size() {
        return size;
    }

    public Set<NamespacedKey> onlyOpenFrom() {
        return onlyOpenFrom;
    }

    public Map<String, String> args() {
        return args;
    }

    public Map<String, SlotFactory> idToItems() {
        return idToItems;
    }

    public YamlMap yaml() {
        return yaml;
    }

    public CashedYamlMap cachedYaml() {
        return cachedYaml;
    }

    public String title() {
        return title;
    }

    public List<SlotFactory> items() {
        return items;
    }

    public @Nullable Animator.AnimatorContext animation() {
        return animation;
    }

    public Map<String, Animator.AnimatorContext> animations() {
        return animations;
    }

    public CommandList commandList() {
        return commandList;
    }

    public Map<String, Commands> eventHandlers() {
        return eventHandlers;
    }

    public long clickCooldown() {
        return clickCooldown;
    }

    public Object data() {
        return data;
    }

    public MenuLoader loader() {
        return loader;
    }

    public List<File> fromFiles() {
        return fromFiles;
    }

    @Override
    public @Nullable SlotFactory resolveSlotBuilder(String name, Menu menu) {
        return idToItems.get(name);
    }

    @Override
    public NamespacedKey getKey() {
        return id;
    }
}
