package dev.by1337.bmenu;

import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.Placeholders;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.bukkit.inventory.ItemStack;
import dev.by1337.bmenu.click.ClickHandler;
import dev.by1337.bmenu.click.ClickHandlerImpl;
import dev.by1337.bmenu.click.MenuClickType;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.factory.ItemFactory;
import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.factory.fixer.ItemFixer;
import dev.by1337.bmenu.item.ItemModel;
import dev.by1337.bmenu.item.MenuItemTickListener;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.requirement.Requirements;
import dev.by1337.bmenu.util.MenuPlaceholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class MenuItemBuilder implements Comparable<MenuItemBuilder> {

    public static final YamlCodec<MenuItemBuilder> YAML_CODEC;

    private int[] slots = new int[]{-1};
    private Map<MenuClickType, ClickHandler> clicks = new HashMap<>();
    private ViewRequirement viewRequirement = ViewRequirement.EMPTY;
    private int priority = 0;
    private Map<String, String> args;
    private MenuItemTickListener tickListener;
    private int tickSpeed;
    private boolean staticItem;
    //private ItemStack staticInstance;
    private MenuPlaceholders localArgs = new MenuPlaceholders(new LinkedHashMap<>(), false);
    private ItemModel itemModel;

    public MenuItemBuilder() {
    }

    public static MenuItemBuilder read(YamlMap yamlMap) {
        return ItemFactory.readItem(yamlMap);
    }

    @Nullable
    public MenuItem build(Menu menu) {
        return build(menu, null, new Placeholders<>());
    }

    @Nullable
    public MenuItem build(Menu menu, @Nullable final ItemStack itemStack, PlaceholderResolver<Menu> resolver1) {
        PlaceholderResolver<Menu> resolver = localArgs == null || localArgs.isEmpty() ? resolver1 : resolver1.and(localArgs);
        var placeholders = resolver.bind(menu);
        if (!viewRequirement.requirement.test(menu, placeholders, menu.getViewer(), ExecuteContext.of(menu))) {
            viewRequirement.denyCommands.run(ExecuteContext.of(menu), placeholders);
            return null;
        }


        if (staticItem && itemStack == null) {
            //todo
        }

        MenuItem item = new MenuItem(
                itemModel,
                clicks,
                tickListener,
                localArgs);
        item.setTickSpeed(tickSpeed);
        return item;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args;
    }

    @Override
    public int compareTo(@NotNull MenuItemBuilder o) {
        return Integer.compare(priority, o.priority);
    }

    public void setClicks(Map<MenuClickType, ClickHandler> clicks) {
        this.clicks = clicks;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }


    public void setViewRequirement(Requirements requirement, Commands denyCommands) {
        this.viewRequirement = new ViewRequirement(requirement, denyCommands);
    }

    public void setViewRequirement(ViewRequirement viewRequirement) {
        this.viewRequirement = viewRequirement;
    }

    public void addClickListener(MenuClickType type, ClickHandler handler) {
        clicks.put(type, handler);
    }

    public ClickHandlerImpl getClickHandlerImplOrNull(MenuClickType type) {
        var v = clicks.get(type);
        if (v instanceof ClickHandlerImpl impl) return impl;
        return null;
    }

    public void setSlots(int[] slots) {
        this.slots = slots;
    }

    @Deprecated(forRemoval = true)
    public void setTicking(boolean ticking) {
        if (ticking && tickListener != null) tickListener = MenuItemTickListener.DEFAULT;
    }

    public void setTickSpeed(int tickSpeed) {
        this.tickSpeed = tickSpeed;
    }

    public void addSlot(final int slot) {
        int[] newSlots = new int[slots.length + 1];
        System.arraycopy(slots, 0, newSlots, 0, slots.length);
        newSlots[slots.length] = slot;
        slots = newSlots;
    }

    public int[] getSlots() {
        return slots;
    }

    public Map<MenuClickType, ClickHandler> getClicks() {
        return clicks;
    }

    public ViewRequirement getViewRequirement() {
        return viewRequirement;
    }

    public int getPriority() {
        return priority;
    }

    @Deprecated(forRemoval = true)
    public boolean isTicking() {
        return tickListener != null;
    }

    public boolean isStaticItem() {
        return staticItem;
    }

    public void setStaticItem(boolean staticItem) {
        this.staticItem = staticItem;
    }

    public static class ViewRequirement {
        private static final ViewRequirement EMPTY = new ViewRequirement(Requirements.EMPTY, Commands.EMPTY);
        public static YamlCodec<ViewRequirement> CODEC = RecordYamlCodecBuilder.mapOf(
                ViewRequirement::new,
                Requirements.CODEC.fieldOf("requirements", ViewRequirement::getRequirement, Requirements.EMPTY),
                Commands.CODEC.fieldOf("deny_commands", ViewRequirement::getDenyCommands, Commands.EMPTY)
        );

        private final Requirements requirement;
        private final Commands denyCommands;

        public ViewRequirement(Requirements requirement, Commands denyCommands) {
            this.requirement = requirement;
            this.denyCommands = denyCommands;
        }

        public Requirements getRequirement() {
            return requirement;
        }

        public Commands getDenyCommands() {
            return denyCommands;
        }

        public boolean isEmpty() {
            return requirement.isEmpty();
        }
    }

    public int[] slots() {
        return slots;
    }


    public Map<MenuClickType, ClickHandler> clicks() {
        return clicks;
    }

    public ViewRequirement viewRequirement() {
        return viewRequirement;
    }


    public int priority() {
        return priority;
    }

    public Map<String, String> args() {
        return args;
    }

    @Deprecated(forRemoval = true)
    public boolean ticking() {
        return isTicking();
    }

    public int tickSpeed() {
        return tickSpeed;
    }

    public MenuItemTickListener getTickListener() {
        return tickListener;
    }

    public void setTickListener(MenuItemTickListener tickListener) {
        this.tickListener = tickListener;
    }

    public MenuPlaceholders getLocalArgs() {
        return localArgs;
    }

    public void setLocalArgs(MenuPlaceholders localArgs) {
        this.localArgs = localArgs;
    }

    static {
        var builder = PipelineYamlCodecBuilder.of(MenuItemBuilder::new)
                .field(ItemModel.CODEC, null, m -> m.itemModel, (m, v) -> m.itemModel = (ItemModel) v)
                .field(MenuCodecs.ARGS_CODEC, "args", MenuItemBuilder::args, MenuItemBuilder::setArgs, Map.of())
                .field(MenuPlaceholders.CODEC, "local_args", MenuItemBuilder::getLocalArgs, MenuItemBuilder::setLocalArgs)
                .field(MenuItemTickListener.CODEC, "on_tick", MenuItemBuilder::getTickListener, MenuItemBuilder::setTickListener)
                .bool("static", MenuItemBuilder::isStaticItem, MenuItemBuilder::setStaticItem, false)
                .integer("priority", MenuItemBuilder::priority, MenuItemBuilder::setPriority, 0)
                .field(YamlCodec.INT.schema(s -> s.or(SchemaTypes.pattern("^\\$\\{[^}]+\\}$"))), "tick_speed", MenuItemBuilder::tickSpeed, MenuItemBuilder::setTickSpeed, 1)
                .field(ItemFactory.SLOTS_YAML_CODEC, "slot", MenuItemBuilder::slots, MenuItemBuilder::setSlots, new int[]{-1})
                .field(ViewRequirement.CODEC, "view_requirement", MenuItemBuilder::viewRequirement, MenuItemBuilder::setViewRequirement);
        for (MenuClickType value : MenuClickType.values()) {
            String key = value.getConfigKeyClick();
            builder.field(ClickHandlerImpl.CODEC, key, m -> m.getClickHandlerImplOrNull(value), (m, v) -> m.addClickListener(value, v));
        }
        final var codec = builder.build();

        YAML_CODEC = new YamlCodec<>() {

            @Override
            public DataResult<MenuItemBuilder> decode(YamlValue yamlValue) {
                ItemFixer.fixItem(yamlValue.asYamlMap().getOrThrow());
                return codec.decode(yamlValue);
            }

            @Override
            public YamlValue encode(MenuItemBuilder menuItemBuilder) {
                return codec.encode(menuItemBuilder);
            }

            @Override
            public @NotNull SchemaType schema() {
                return codec.schema();
            }
        };
    }
}
