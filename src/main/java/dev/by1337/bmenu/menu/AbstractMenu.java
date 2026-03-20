package dev.by1337.bmenu.menu;

import dev.by1337.bmenu.MenuEvents;
import dev.by1337.bmenu.animation.Animator;
import dev.by1337.bmenu.command.Commands;
import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.handler.MenuEventHandler;
import dev.by1337.bmenu.inventory.BukkitInventory;
import dev.by1337.bmenu.loader.MenuConfig;
import dev.by1337.bmenu.loader.MenuLoader;
import dev.by1337.bmenu.menu.command.MenuCommands;
import dev.by1337.bmenu.placeholder.PlaceholderResolverList;
import dev.by1337.bmenu.placeholder.SimplePlaceholders;
import dev.by1337.bmenu.requirement.Requirement;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.bmenu.slot.SlotFactory;
import dev.by1337.bmenu.slot.component.MenuClickType;
import dev.by1337.bmenu.util.StringWatcher;
import dev.by1337.bmenu.util.math.FastExpressionParser;
import dev.by1337.cmd.Command;
import dev.by1337.core.util.text.MessageFormatter;
import dev.by1337.core.util.text.minimessage.MiniMessage;
import dev.by1337.plc.PapiResolver;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.Placeholders;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.IntFunction;

public abstract class AbstractMenu implements Menu {
    private static final Logger log = LoggerFactory.getLogger("BMenu");
    private static final Random rand = new Random();
    private static final DecimalFormat df = new DecimalFormat("#.##");
    private static final PlaceholderResolver<Menu> MENU_PLACEHOLDERS = Placeholders.<Menu>create()
            .withContext("menu_id", m -> Objects.requireNonNullElse(m.config().id(), NamespacedKey.minecraft("unnamed")))
            .withContext("has_back_menu", m -> m.previousMenu() != null)
            .withContext("clicked_slot", Menu::lastClickedSlot)
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
            .and(PapiResolver.INSTANCE.map(m -> m.viewer()));


    protected final MenuConfig config;
    protected final MenuLoader loader;
    protected final Player viewer;
    protected final MenuMatrix layers;
    protected final Map<String, String> args;
    @Nullable
    protected final Menu previousMenu;
    @Nullable
    protected Menu upperMenu;
    protected Animator animator;
    protected @Nullable SlotContent lastClickedItem;
    protected long lastClickTime;
    protected int lastClickedSlot = 0;
    protected long clickCooldown;
    private final PlaceholderResolverList resolvers;
    protected final SimplePlaceholders argsPlaceholders;
    private BukkitInventory inventoryLike;
    protected final StringWatcher title;

    public AbstractMenu(MenuConfig config, Player viewer, @Nullable Menu previousMenu) {
        argsPlaceholders = new SimplePlaceholders();
        resolvers = new PlaceholderResolverList();
        resolvers.addResolver(MENU_PLACEHOLDERS);
        resolvers.addResolver(argsPlaceholders);
        clickCooldown = config.clickCooldown();
        this.config = config;
        title = new StringWatcher(config.title(), s -> {
            inventoryLike.setTitle(MiniMessage.deserialize(s));
        });
        loader = config.loader();
        this.viewer = viewer;
        layers = new MenuMatrix(menuSize(), this);
        args = new HashMap<>(config.args());
        this.previousMenu = previousMenu;
        args.keySet().forEach(k -> argsPlaceholders.set(k, () -> args.get(k)));
    }

    private int menuSize() {
        if (config.invType() == InventoryType.CHEST) return config.size();
        return config.invType().getDefaultSize();
    }

    public void open() {
        open(false);
    }

    public void open(boolean isReopen) {
        if (!config.canOpenFrom(previousMenu)) {
            throw new IllegalStateException(
                    MessageFormatter.apply(
                            "It is not possible to open menu {} from menu {}, only-open-from: {}",
                            config.id(),
                            previousMenu == null ? "NONE" : previousMenu.config().id(),
                            config.onlyOpenFrom()
                    )
            );
        }
        if (!viewer.isOnline()) {
            throw new IllegalStateException("Player is not online");
        }
        if (inventoryLike == null) {
            inventoryLike = new BukkitInventory(
                    this,
                    config.size(),
                    MiniMessage.deserialize(setPlaceholders(title.data())),
                    config.invType()
            );
        }
        inventoryLike.clear();
        inventoryLike.show(viewer);
        if (isReopen && animator != null) {
            animator.setPos(0);
        }
        if (animator == null && config.animation() != null) {
            animator = config.animation().createAnimator();
        }
        layers.clear();
        onEvent(isReopen ? MenuEvents.ON_REOPEN : MenuEvents.ON_OPEN);
        if (animator != null && !animator.isEnd()) {
            animator.tick(layers.getAnimationLayer(), this);
        }
        if (!Objects.equals(viewer.getOpenInventory().getTopInventory(), inventoryLike.getInventory())) {
            return;
        }
        generate0();
    }

    public void tick() {
        if (animator != null && !animator.isEnd()) {
            animator.tick(layers.getAnimationLayer(), this);
        }
        layers.doTick();
        flush();
    }

    public void reopen() {
        open(true);
    }


    public void refresh() {
        for (SlotContent item : layers.getAnimationLayer()) {
            if (item == null) continue;
            item.setDirty(true);
        }
        generate0();
    }

    public void rebuildItemsInSlots(int[] slots) {
        for (int slot : slots) {
            SlotContent item;
            if ((item = layers.getItemInSlot(slot)) != null) {
                item.setDirty(true);
            }
        }
    }

    protected abstract void generate();

    protected void generate0() {
        Arrays.fill(layers.getBaseLayer(), null);
        config.fill(this, layers.getBaseLayer());
        generate();
        onEvent(MenuEvents.ON_REFRESH);
        title.setData(setPlaceholders(config.title()));
        flush();
    }

    public void close() {
        viewer.closeInventory();
    }

    public void flush() {
        layers.flushTo(inventoryLike.getItems());
        syncItems();
    }

    protected void syncItems() {
        inventoryLike.sync(viewer);
    }

    @Override
    public String setPlaceholders(String string) {
        return resolvers.setPlaceholders(string, this);
    }

    @Override
    public void runCommands(ExecuteContext ctx, List<String> commands) {
        for (String command : commands) {
            executeCommand(ctx, setPlaceholders(command));
        }
    }

    @Override
    public @Nullable SlotFactory resolveSlotBuilder(String name, Menu menu) {
        return config.resolveSlotBuilder(name, menu);
    }

    public void onClose(InventoryCloseEvent event) {
        inventoryLike.onClose(viewer);
        onEvent(MenuEvents.ON_CLOSE);
    }

    public void onClick(InventoryDragEvent e) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < clickCooldown) return;
        lastClickTime = now;
    }

    public void onClick(InventoryClickEvent e) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < clickCooldown) return;
        lastClickTime = now;

        if (e.getCurrentItem() == null) {
            return;
        }
        if (!Objects.equals(inventoryLike.getInventory(), e.getClickedInventory())) return;
        lastClickedSlot = e.getSlot();
        SlotContent item = findItemInSlot(e.getSlot());
        if (item == null) return;
        lastClickedItem = item;
        MenuClickType type = MenuClickType.getClickType(e);
        item.doClick(this, viewer, type);
    }

    @Nullable
    public SlotContent findItemInSlot(int slot) {
        return layers.getItemInSlot(slot);
    }

    public void setTitle(String title) {
        this.title.setData(title);
    }

    public void onEvent(String event) {
        Commands commandRequirements = config.eventHandlers().get(event);
        if (commandRequirements != null) {
            try (var ctx = ExecuteContext.of(this, event)){
                commandRequirements.test(ctx, this);
            }
        }
    }

    public PlaceholderResolverList resolvers() {
        return resolvers;
    }

    @Override
    @Deprecated
    public @NotNull Inventory getInventory() {
        return inventoryLike.getInventory();
    }

    public SlotContent[] matrix() {
        return layers.getBaseLayer();
    }

    public SlotContent[] animationMask() {
        return layers.getAnimationLayer();
    }


    public @Nullable SlotContent lastClickedItem() {
        return lastClickedItem;
    }

    public long lastClickTime() {
        return lastClickTime;
    }

    public void addArgument(String key, String value) {
        args.put(key, value);
        argsPlaceholders.set(key, () -> args.get(key));
    }

    public void addPlaceholderResolver(PlaceholderResolver<Menu> resolver) {
        resolvers.addResolver(resolver);
    }

    @Override
    public String toString() {
        return "Menu{" +
                "loader=" + loader +
                ", config=" + config +
                ", viewer=" + viewer +
                '}';
    }

    private static <R> @Nullable R tryToInt(String in, IntFunction<R> func) {
        try {
            double d = FastExpressionParser.parse(in);
            return func.apply((int) d);
        } catch (FastExpressionParser.MathFormatException e) {
            return null;
        }
    }

    public NamespacedKey getId() {
        return config.id();
    }

    public @Nullable Menu upperMenu() {
        return upperMenu;
    }

    public void setUpperMenu(@Nullable Menu upperMenu) {
        this.upperMenu = upperMenu;
    }

    public @Nullable Menu previousMenu() {
        return previousMenu;
    }


    public MenuMatrix layers() {
        return layers;
    }

    @Override
    public Command<ExecuteContext> getCommands() {
        return MenuCommands.getCommands();
    }

    public MenuConfig config() {
        return config;
    }

    public MenuLoader loader() {
        return loader;
    }

    public Player viewer() {
        return viewer;
    }

    public Animator animator() {
        return animator;
    }

    public void setAnimator(Animator animator) {
        if (this.animator != null) {
            this.animator.forceEnd(animationMask(), this);
        }
        this.animator = animator;
    }

    public int lastClickedSlot() {
        return lastClickedSlot;
    }
}
//    /**
//     * Определяет, поддерживает ли меню горячую перезагрузку.
//     * Если возвращает {@code true}, то при горячей перезагрузке будет создан новый экземпляр меню,
//     * а также новый экземпляр {@link Menu#previousMenu}.
//     *
//     * @return {@code true}, если меню поддерживает горячую перезагрузку, иначе {@code false}.
//     */
//    public boolean isSupportsHotReload() {
//        return false;
//    }
//
//    /**
//     * Вызывается при горячей перезагрузке меню. Позволяет перенести данные из старого экземпляра в новый.
//     * Этот метод вызывается только если {@link #isSupportsHotReload()} возвращает {@code true}.
//     *
//     * @param oldMenu старый экземпляр меню, из которого можно перенести данные в новый.
//     */
//    public void onHotReload(@NotNull Menu oldMenu) {
//        args.putAll(oldMenu.args);
//        for (String param : args.keySet()) {
//            argsPlaceholders.set(param, () -> args.get(param));
//        }
//    }