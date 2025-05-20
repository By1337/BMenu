package org.by1337.bmenu;

import dev.by1337.yaml.BukkitYamlCodecs;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import dev.by1337.yaml.codec.schema.SchemaTypes;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
import org.by1337.blib.chat.placeholder.MultiPlaceholder;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.util.Pair;
import org.by1337.bmenu.click.ClickHandler;
import org.by1337.bmenu.click.ClickHandlerImpl;
import org.by1337.bmenu.click.MenuClickType;
import org.by1337.bmenu.factory.ItemFactory;
import org.by1337.bmenu.factory.MenuCodecs;
import org.by1337.bmenu.item.MenuItemTickListener;
import org.by1337.bmenu.requirement.Requirements;
import org.by1337.bmenu.util.CachedComponent;
import org.by1337.bmenu.util.ItemStackBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class MenuItemBuilder implements Comparable<MenuItemBuilder> {

    public static final YamlCodec<MenuItemBuilder> YAML_CODEC;

    private int[] slots = new int[]{-1};
    private final List<CachedComponent> lore = new ArrayList<>();
    private CachedComponent name;
    private Map<MenuClickType, ClickHandler> clicks = new HashMap<>();
    private String amount = "1";
    private String material = "STONE";
    private ViewRequirement viewRequirement = ViewRequirement.EMPTY;
    private int modelData = 0;
    private final Set<ItemFlag> itemFlags = EnumSet.noneOf(ItemFlag.class);
    private List<PotionEffect> potionEffects = new ArrayList<>();
    private Color color = null;
    private int priority = 0;
    private List<Pair<Enchantment, Integer>> enchantments = new ArrayList<>();
    private boolean unbreakable;
    private String damage;
    private Map<String, String> args;
    private MenuItemTickListener tickListener;
    private int tickSpeed;
    private boolean staticItem;
    private ItemStack staticInstance;
    private boolean hideTooltip;
    private ItemStackBuilder itemStackBuilder;

    public MenuItemBuilder() {
    }

    @Deprecated(forRemoval = true)
    @SuppressWarnings("removal")
    public static MenuItemBuilder read(YamlContext context, MenuLoader loader) {
        return ItemFactory.readItem(context, loader);
    }

    public static MenuItemBuilder read(YamlMap yamlMap) {
        return ItemFactory.readItem(yamlMap);
    }

    @Nullable
    public MenuItem build(Menu menu) {
        return build(menu, null);
    }

    @Nullable
    public MenuItem build(Menu menu, @Nullable final ItemStack itemStack, Placeholderable... placeholderables) {
        MultiPlaceholder placeholder = new MultiPlaceholder(placeholderables);
        placeholder.add(menu);
        if (!viewRequirement.requirement.test(menu, placeholder, menu.viewer)) {
            menu.runCommands(viewRequirement.denyCommands);
            return null;
        }
        if (itemStackBuilder == null) itemStackBuilder = new ItemStackBuilder(this);


        if (staticItem) {
            staticInstance = itemStackBuilder.build(itemStack, menu.loader.getMessage(), placeholder);
        }

        Supplier<@Nullable MenuItem> builder = () -> build(menu, itemStack, placeholderables);
        MenuItem item = new MenuItem(
                slots,
                (t) -> staticInstance != null ? staticInstance : itemStackBuilder.build(itemStack, menu.loader.getMessage(), new BiPlaceholder(placeholder, t)),
                clicks,
                tickListener,
                builder
        );
        item.setTickSpeed(tickSpeed);
        return item;
    }

    public void setDamage(int damage) {
        this.damage = String.valueOf(damage);
    }

    public void setDamage(String damage) {
        this.damage = damage;
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

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public void setClicks(Map<MenuClickType, ClickHandler> clicks) {
        this.clicks = clicks;
    }

    public void setEnchantments(List<Pair<Enchantment, Integer>> enchantments) {
        this.enchantments = enchantments;
    }

    public void addEnchantment(Enchantment enchantment, int level) {
        enchantments.add(new Pair<>(enchantment, level));
    }

    public void setName(String name) {
        this.name = new CachedComponent(name);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setItemFlags(List<ItemFlag> itemFlags) {
        this.itemFlags.addAll(itemFlags);
    }

    public void setPotionEffects(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public void addPotionEffect(final PotionEffect effect) {
        potionEffects.add(effect);
    }

    public void addItemFlag(ItemFlag flag) {
        itemFlags.add(flag);
    }

    public void setModelData(int modelData) {
        this.modelData = modelData;
    }

    public void setViewRequirement(Requirements requirement, List<String> denyCommands) {
        this.viewRequirement = new ViewRequirement(requirement, denyCommands);
    }

    public void setViewRequirement(ViewRequirement viewRequirement) {
        this.viewRequirement = viewRequirement;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void addClickListener(MenuClickType type, ClickHandler handler) {
        clicks.put(type, handler);
    }

    public ClickHandlerImpl getClickHandlerImplOrNull(MenuClickType type) {
        var v = clicks.get(type);
        if (v instanceof ClickHandlerImpl impl) return impl;
        return null;
    }

    public String getName() {
        return name.getSource();
    }


    public void setLore(List<String> lore) {
        this.lore.clear();
        for (String s : lore) {
            s = s.replace("\\n", "\n");
            if (s.contains("\n")) {
                for (String string : s.split("\n")) {
                    this.lore.add(new CachedComponent(string));
                }
            } else {
                this.lore.add(new CachedComponent(s));
            }
        }
    }

    public void addLore(String lore) {
        this.lore.add(new CachedComponent(lore));
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

    public List<String> getLore() {
        return lore();
    }

    public Map<MenuClickType, ClickHandler> getClicks() {
        return clicks;
    }

    public String getAmount() {
        return amount;
    }

    public String getMaterial() {
        return material;
    }

    public ViewRequirement getViewRequirement() {
        return viewRequirement;
    }

    public int getModelData() {
        return modelData;
    }

    public List<ItemFlag> getItemFlags() {
        return itemFlags.stream().toList();
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public Color getColor() {
        return color;
    }

    public int getPriority() {
        return priority;
    }

    public List<Pair<Enchantment, Integer>> getEnchantments() {
        return enchantments;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public String getDamage() {
        return damage;
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
        private static final ViewRequirement EMPTY = new ViewRequirement(Requirements.EMPTY, Collections.emptyList());
        public static YamlCodec<ViewRequirement> CODEC = RecordYamlCodecBuilder.mapOf(
                Requirements.CODEC.fieldOf("requirements", ViewRequirement::getRequirement, Requirements.EMPTY),
                YamlCodec.STRINGS.fieldOf("deny_commands", ViewRequirement::getDenyCommands, List.of()),
                ViewRequirement::new
        );

        private final Requirements requirement;
        private final List<String> denyCommands;

        public ViewRequirement(Requirements requirement, List<String> denyCommands) {
            this.requirement = requirement;
            this.denyCommands = denyCommands;
        }

        public Requirements getRequirement() {
            return requirement;
        }

        public List<String> getDenyCommands() {
            return denyCommands;
        }

        public boolean isEmpty() {
            return requirement.isEmpty();
        }
    }

    public int[] slots() {
        return slots;
    }

    public List<String> lore() {
        return lore.stream().map(CachedComponent::getSource).toList();
    }

    public String name() {
        return name.getSource();
    }

    public Map<MenuClickType, ClickHandler> clicks() {
        return clicks;
    }

    public String amount() {
        return amount;
    }

    public String material() {
        return material;
    }

    public ViewRequirement viewRequirement() {
        return viewRequirement;
    }

    public int modelData() {
        return modelData;
    }

    public List<ItemFlag> itemFlags() {
        return itemFlags.stream().toList();
    }

    public List<PotionEffect> potionEffects() {
        return potionEffects;
    }

    public Color color() {
        return color;
    }

    public int priority() {
        return priority;
    }

    public List<Pair<Enchantment, Integer>> enchantments() {
        return enchantments;
    }

    public boolean unbreakable() {
        return unbreakable;
    }

    public String damage() {
        return damage;
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

    public boolean hideTooltip() {
        return hideTooltip;
    }

    public void setHideTooltip(boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
    }

    @ApiStatus.Internal
    public void setAllItemFlags(boolean b) {
        if (!b) return;
        setItemFlags(Arrays.stream(ItemFlag.values()).toList());
    }

    public boolean isAllItemFlags() {
        return itemFlags.size() == ItemFlag.values().length;
    }

    public CachedComponent getCashedName() {
        return name;
    }

    public List<CachedComponent> getCashedLore() {
        return lore;
    }

    public MenuItemTickListener getTickListener() {
        return tickListener;
    }

    public void setTickListener(MenuItemTickListener tickListener) {
        this.tickListener = tickListener;
    }

    static {
        var builder = PipelineYamlCodecBuilder.of(MenuItemBuilder::new)
                .field(MenuCodecs.MATERIAL, "material", b -> b.material, (b, m) -> b.material = m, "stone")
                .string("name", MenuItemBuilder::name, MenuItemBuilder::setName)
                .field(YamlCodec.STRING.schema(s -> s.or(SchemaTypes.INT)), "amount", MenuItemBuilder::amount, MenuItemBuilder::setAmount, "1")
                .strings("lore", MenuItemBuilder::lore, MenuItemBuilder::setLore, List.of())
                .field(MenuCodecs.ARGS_CODEC, "args", MenuItemBuilder::args, MenuItemBuilder::setArgs, Map.of())
                .bool("unbreakable", MenuItemBuilder::unbreakable, MenuItemBuilder::setUnbreakable, false)
                //.bool("ticking", MenuItemBuilder::ticking, MenuItemBuilder::setTicking, false)
                .field(MenuItemTickListener.CODEC, "on_tick", MenuItemBuilder::getTickListener, MenuItemBuilder::setTickListener)
                .bool("static", MenuItemBuilder::isStaticItem, MenuItemBuilder::setStaticItem, false)
                .bool("all_flags", MenuItemBuilder::isAllItemFlags, MenuItemBuilder::setAllItemFlags, false)
                .integer("model_data", MenuItemBuilder::modelData, MenuItemBuilder::setModelData, 0)
                .integer("priority", MenuItemBuilder::priority, MenuItemBuilder::setPriority, 0)
                .string("damage", MenuItemBuilder::damage, MenuItemBuilder::setDamage, null)
                .field(YamlCodec.INT.schema(s -> s.or(SchemaTypes.pattern("^\\$\\{[^}]+\\}$"))), "tick_speed", MenuItemBuilder::tickSpeed, MenuItemBuilder::setTickSpeed, 1)
                .field(BukkitYamlCodecs.COLOR, "color", MenuItemBuilder::color, MenuItemBuilder::setColor)
                .listOf(BukkitYamlCodecs.ITEM_FLAG, "item_flags", MenuItemBuilder::getItemFlags, MenuItemBuilder::setItemFlags, List.of())
                .field(ItemFactory.SLOTS_YAML_CODEC, "slot", MenuItemBuilder::slots, MenuItemBuilder::setSlots, new int[]{-1})
                .listOf(MenuCodecs.ENCHANTMENT_YAML_CODEC, "enchantments", MenuItemBuilder::enchantments, MenuItemBuilder::setEnchantments, List.of())
                .listOf(MenuCodecs.POTION_EFFECT_YAML_CODEC, "potion_effects", MenuItemBuilder::potionEffects, MenuItemBuilder::setPotionEffects, List.of())
                .field(ViewRequirement.CODEC, "view_requirement", MenuItemBuilder::viewRequirement, MenuItemBuilder::setViewRequirement);
        for (MenuClickType value : MenuClickType.values()) {
            String key = value.getConfigKeyClick();
            builder.field(ClickHandlerImpl.CODEC, key, m -> m.getClickHandlerImplOrNull(value), (m, v) -> m.addClickListener(value, v));
        }
        YAML_CODEC = builder.build();
    }
}
