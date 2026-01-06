package org.by1337.bmenu.menu;

import dev.by1337.cmd.Command;
import dev.by1337.cmd.CommandMsgError;
import dev.by1337.cmd.CompiledCommand;
import dev.by1337.cmd.argument.ArgumentCommand;
import dev.by1337.cmd.argument.ArgumentString;
import dev.by1337.cmd.argument.ArgumentStrings;
import dev.by1337.core.command.bcmd.CommandError;
import dev.by1337.core.command.bcmd.argument.ArgumentComponents;
import dev.by1337.core.command.bcmd.argument.ArgumentDouble;
import dev.by1337.core.command.bcmd.argument.ArgumentInt;
import dev.by1337.core.command.bcmd.argument.ArgumentRegistry;
import dev.by1337.core.util.text.MessageFormatter;
import dev.by1337.core.util.text.minimessage.MiniMessage;
import dev.by1337.plc.PapiResolver;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.Placeholderable;
import dev.by1337.plc.Placeholders;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.codec.YamlCodec;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;
import org.by1337.bmenu.*;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.util.AnimationUtil;
import org.by1337.bmenu.click.MenuClickType;
import org.by1337.bmenu.command.CommandRunner;
import org.by1337.bmenu.command.Commands;
import org.by1337.bmenu.command.ExecuteContext;
import org.by1337.bmenu.hook.VaultHook;
import org.by1337.bmenu.inventory.BukkitInventory;
import org.by1337.bmenu.network.BungeeCordMessageSender;
import org.by1337.bmenu.requirement.CommandRequirements;
import org.by1337.bmenu.util.math.FastExpressionParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.IntFunction;

public abstract class Menu implements InventoryHolder, CommandRunner<ExecuteContext>, Placeholderable {
    private static final Command<ExecuteContext> commands;
    private static final Logger log = LoggerFactory.getLogger("BMenu");
    private static final Random rand = new Random();
    private static final DecimalFormat df = new DecimalFormat("#.##");
    private static final PlaceholderResolver<Menu> MENU_PLACEHOLDERS = Placeholders.<Menu>create()
            .withContext("has_back_menu", m -> m.previousMenu != null)
            .withContext("clicked_slot", m -> m.lastClickedSlot)
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
            .and(PapiResolver.INSTANCE.map(m -> m.viewer));


    protected final MenuConfig config;
    protected final MenuLoader loader;
    protected final Player viewer;
    protected final MenuMatrix layers;
    protected final Map<String, String> args;
    @Nullable
    protected final Menu previousMenu;
    protected BukkitTask ticker;
    protected Animator animator;
    protected @Nullable MenuItem lastClickedItem;
    protected long lastClickTime;
    protected String lastTitle;
    protected int lastClickedSlot = 0;
    protected long clickCooldown;
    protected final Placeholders<Menu> runtimePlaceholders;
    private final PlaceholderResolver<Menu> placeholderResolver;
    private BukkitInventory inventoryLike;

    public Menu(MenuConfig config, Player viewer, @Nullable Menu previousMenu) {
        runtimePlaceholders = new Placeholders<>();
        placeholderResolver = MENU_PLACEHOLDERS.and(runtimePlaceholders);
        clickCooldown = config.clickCooldown();
        this.config = config;
        loader = config.getLoader();
        this.viewer = viewer;
        layers = new MenuMatrix(menuSize(), this);
        args = new HashMap<>(config.getArgs());
        this.previousMenu = previousMenu;
        args.keySet().forEach(k -> runtimePlaceholders.of(k, () -> args.get(k)));
    }

    public int menuSize() {
        if (config.getInvType() == InventoryType.CHEST) return config.getSize();
        return config.getInvType().getDefaultSize();
    }

    public void open() {
        open(false);
    }

    public void open(boolean isReopen) {
        if (!config.canOpenFrom(previousMenu)) {
            throw new IllegalStateException(
                    MessageFormatter.apply(
                            "It is not possible to open menu {} from menu {}, only-open-from: {}",
                            config.getId(),
                            previousMenu == null ? "NONE" : previousMenu.config.getId(),
                            config.getOnlyOpenFrom()
                    )
            );
        }
        if (!viewer.isOnline()) {
            throw new IllegalStateException("Player is not online");
        }
        if (inventoryLike == null) {
            inventoryLike = new BukkitInventory(
                    createInventory(config.getSize(), MiniMessage.deserialize(replace(config.getTitle())), config.getInvType()),
                    this
            );

        }
        sync(() -> {
            inventoryLike.clear();
            inventoryLike.show(viewer);

            if (animator == null && config.getAnimation() != null) {
                animator = config.getAnimation().createAnimator();
            }
            onEvent(isReopen ? MenuEvents.ON_REOPEN : MenuEvents.ON_OPEN);

            if (!Objects.equals(viewer.getOpenInventory().getTopInventory(), inventoryLike.getInventory())) {
                return;
            }
            layers.clear();
            if (animator != null && !animator.isEnd()) {
                animator.tick(layers.getAnimationLayer(), this);
            }
            generate0();

            if (ticker != null && !ticker.isCancelled()) {
                ticker.cancel();
            }
            ticker = Bukkit.getScheduler().runTaskTimer(
                    loader.getPlugin(),
                    this::tick,
                    1,
                    1
            );
        });
    }

    protected void tick() {
        if (animator != null && !animator.isEnd()) {
            animator.tick(layers.getAnimationLayer(), this);
        }
        layers.doTick();
        flush();
    }

    public void reopen() {
        if (ticker != null && !ticker.isCancelled()) {
            ticker.cancel();
        }
        if (animator != null) {
            animator.setPos(0);
        }
        open(true);
    }

    public void refresh() {//todo!
        for (MenuItem item : layers.getAnimationLayer()) {
            if (item == null) continue;
            item.setDirty(true);
        }
        generate0();
    }

    public void rebuildItemsInSlots(int[] slots) {
        for (int slot : slots) {
            MenuItem item;
            if ((item = layers.getItemInSlot(slot)) != null) { //todo!
                item.setDirty(true);
            }
        }
    }

    protected abstract void generate();

    protected void generate0() {
        Arrays.fill(layers.getBaseLayer(), null);
        config.generate(this);
        generate();
        onEvent(MenuEvents.ON_REFRESH);
        updateTitle();
        flush();
    }

    public void close() {
        viewer.closeInventory();
    }

    protected void flush() {
        layers.flushTo(inventoryLike.getItems());
        syncItems();
    }

    protected void syncItems() {
        inventoryLike.sync(viewer);
    }

    protected void setMatrix(MenuItem[] mask) {
        for (int i = 0; i < mask.length; i++) {
            MenuItem item = mask[i];
            if (item != null) {
                inventoryLike.setItem(i, item);
            }
        }
    }

    public void setItem(MenuItem item, int slot) {
        setItem(item, slot, layers.getBaseLayer());
    }

    public void setItem(MenuItem item, int slot, MenuItem[] matrix) {
        if (slot == -1) return;
        if (slot < 0 || matrix.length < slot) {
            loader.getLogger().error("Slot {} is out of bounds! Menu: {}", slot, config.getId());
        } else {
            matrix[slot] = item;
        }
    }

    protected Inventory createInventory(int size, Component title, InventoryType type) {
        if (type == InventoryType.CHEST) {
            return Bukkit.createInventory(this, size, title);
        } else {
            return Bukkit.createInventory(this, type, title);
        }
    }

    @Override
    public String replace(String string) {
        return placeholderResolver.replace(string, this);
    }

    protected abstract boolean runCommand(String cmd) throws CommandMsgError;

    @Override
    public void runCommands(ExecuteContext ctx, List<String> commands) {
        for (String command : commands) {
            executeCommand(ctx, replace(command));
        }
    }

    @Override
    public @Nullable CompiledCommand<ExecuteContext> executeAndTryCompile(ExecuteContext ctx, String command) {
        try {
            if (!runCommand(command)) {
                CompiledCommand<ExecuteContext> cmp = Menu.commands.compile(command);
                if (cmp != null) {
                    cmp.execute(ctx);
                    return cmp;
                } else {
                    Menu.commands.execute(ctx, command);
                    return null;
                }
            }
        } catch (Exception e) {
            loader.getLogger().error("Failed to run command: {}", command, e);
        }
        return null;
    }

    @Override
    public void executeCommand(ExecuteContext ctx, CompiledCommand<ExecuteContext> command) {
        try {
            log.info("run {} {}", command.getSource(), command);
            command.execute(ctx);
        } catch (Exception e) {
            loader.getLogger().error("Failed to run command: {}", command.getSource(), e);
        }
    }

    @Override
    public final void executeCommand(ExecuteContext ctx, String command) {
        try {
            if (!runCommand(command)) {
                log.info("run {}", command);
                Menu.commands.execute(ctx, command);
            }
        } catch (Exception e) {
            loader.getLogger().error("Failed to run command: {}", command, e);
        }
    }

    public void onClose(InventoryCloseEvent event) {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        inventoryLike.onClose(viewer);

        onEvent(MenuEvents.ON_CLOSE);
    }

    public void onClick(InventoryDragEvent e) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < 100) return;
        lastClickTime = now;
    }

    public void onClick(InventoryClickEvent e) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < 100) return;
        lastClickTime = now;

        if (e.getCurrentItem() == null) {
            return;
        }
        if (!Objects.equals(inventoryLike.getInventory(), e.getClickedInventory())) return;
        lastClickedSlot = e.getSlot();
        MenuItem item = findItemInSlot(e.getSlot());
        if (item == null) return;
        lastClickedItem = item;
        MenuClickType type = MenuClickType.getClickType(e);
        item.doClick(this, viewer, type);
    }

    @Nullable
    public MenuItem findItemInSlot(int slot) {
        return layers.getItemInSlot(slot);
    }

    protected void sync(Runnable runnable) {
        sync(runnable, 0);
    }

    protected void sync(Runnable runnable, int delay) {
        if (!Bukkit.isPrimaryThread() || delay != 0)
            Bukkit.getScheduler().runTaskLater(loader.getPlugin(), runnable, delay);
        else runnable.run();
    }

    public void sendFakeTitle(String title) {
        inventoryLike.setTitle(MiniMessage.deserialize(replace(title)));
    }

    public void updateTitle() {
        String newTitle = replace(config.getTitle());
        if (!Objects.equals(lastTitle, newTitle)) {
            lastTitle = newTitle;
            inventoryLike.setTitle(MiniMessage.deserialize(replace(newTitle)));
        }
    }

    public void setTitle(String title) {
        sendFakeTitle(title);
    }

    public void onEvent(String event) {
        CommandRequirements commandRequirements = config.getMenuEventListeners().get(event);
        if (commandRequirements != null) {
            commandRequirements.run(this, this, viewer);
        }
    }

    public MenuLoader getLoader() {
        return loader;
    }

    public Player getViewer() {
        return viewer;
    }

    @Override
    @Deprecated
    public @NotNull Inventory getInventory() {
        return inventoryLike.getInventory();
    }

    public MenuItem[] getMatrix() {
        return layers.getBaseLayer();
    }

    public MenuItem[] getAnimationMask() {
        return layers.getAnimationLayer();
    }

    public MenuConfig getConfig() {
        return config;
    }

    public @Nullable Menu getPreviousMenu() {
        return previousMenu;
    }

    public @Nullable MenuItem getLastClickedItem() {
        return lastClickedItem;
    }

    public long getLastClickTime() {
        return lastClickTime;
    }

    public void addArgument(String key, String value) {
        args.put(key, value);
        runtimePlaceholders.of(key, () -> args.get(key));
    }

    /**
     * Определяет, поддерживает ли меню горячую перезагрузку.
     * Если возвращает {@code true}, то при горячей перезагрузке будет создан новый экземпляр меню,
     * а также новый экземпляр {@link Menu#previousMenu}.
     *
     * @return {@code true}, если меню поддерживает горячую перезагрузку, иначе {@code false}.
     */
    public boolean isSupportsHotReload() {
        return false;
    }

    /**
     * Вызывается при горячей перезагрузке меню. Позволяет перенести данные из старого экземпляра в новый.
     * Этот метод вызывается только если {@link #isSupportsHotReload()} возвращает {@code true}.
     *
     * @param oldMenu старый экземпляр меню, из которого можно перенести данные в новый.
     */
    public void onHotReload(@NotNull Menu oldMenu) {
        args.putAll(oldMenu.args);
        for (String param : args.keySet()) {
            runtimePlaceholders.of(param, () -> args.get(param));
        }
    }


    public MenuItem[] getLayer(int index) {
        return layers.getMatrix(index);
    }

    public PlaceholderResolver<Menu> getPlaceholderResolver() {
        return placeholderResolver;
    }

    @Override
    public String toString() {
        return "Menu{" +
                "loader=" + loader +
                ", config=" + config +
                ", viewer=" + viewer +
                '}';
    }

    private static void runIn(String rawList, Menu menu, MenuLoader loader, ExecuteContext ctx) {
        if (rawList != null && !rawList.isBlank()) {
            try {
                YamlMap yaml = YamlMap.loadFromString("data: " + rawList);
                var list = yaml.get("data").decode(YamlCodec.STRINGS).getOrThrow();
                menu.runCommands(ctx, list);
            } catch (Exception e) {
                loader.getLogger().error("Failed to parse commands {}", rawList, e);
            }
        }
    }

    private static <R> @Nullable R tryToInt(String in, IntFunction<R> func) {
        try {
            double d = FastExpressionParser.parse(in);
            return func.apply((int) d);
        } catch (FastExpressionParser.MathFormatException e) {
            return null;
        }
    }

    @ApiStatus.Internal
    public static Command<ExecuteContext> getCommands() {
        return commands;
    }

    static {
        commands = new Command<>("root");
        commands.sub(new Command<ExecuteContext>("[CONSOLE]")
                .aliases("[console]")
                .argument(new ArgumentStrings<>("cmd"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("cmd", "use: [console] <cmd>");
                            v.menu.sync(() -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd));
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[PLAYER]")
                .aliases("[player]")
                .argument(new ArgumentStrings<>("cmd"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("cmd", "use: [player] <cmd>");
                            v.menu.sync(() -> v.menu.viewer.performCommand(cmd));
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[CHAT]")
                .aliases("[chat]")
                .argument(new ArgumentStrings<>("cmd"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("cmd", "use: [chat] <cmd>");
                            v.menu.sync(() -> v.menu.viewer.chat(cmd));
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[SOUND]")
                .aliases("[sound]")
                .argument(new ArgumentRegistry<>("sound", Registry.SOUNDS))
                .argument(new ArgumentDouble<>("volume"))
                .argument(new ArgumentDouble<>("pitch"))
                .executor((v, args) -> {
                            float volume = ((Double) args.getOrDefault("volume", 1F)).floatValue();
                            float pitch = ((Double) args.getOrDefault("pitch", 1F)).floatValue();
                            Sound sound = (Sound) args.getOrThrow("sound", "use [sound] <sound> <?volume> <?pitch>");
                            Player reciver = v.menu.viewer;
                            reciver.playSound(reciver.getLocation(), sound, volume, pitch);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[CLOSE]")
                .aliases("[close]")
                .executor((v, args) -> v.menu.sync(v.menu.viewer::closeInventory))
        );
        commands.sub(new Command<ExecuteContext>("[BACK_OR_CLOSE]")
                .aliases("[back_or_close]")
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> v.menu.sync(() -> {
                    if (v.menu.previousMenu != null) {
                        if (args.containsKey("commands"))
                            runIn((String) args.get("commands"), v.menu.previousMenu, v.menu.loader, v);
                        v.menu.previousMenu.reopen();
                    } else {
                        v.menu.viewer.closeInventory();
                    }
                }))
        );
        commands.sub(new Command<ExecuteContext>("[BACK_TO_OR_CLOSE]")
                .aliases("[back_to_or_close]")
                .argument(new ArgumentString<>("id"))
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> {
                    String id = (String) args.getOrThrow("id", "Use: [back_to_or_close] <id>");
                    v.menu.sync(() -> {
                        Menu m = v.menu.previousMenu;
                        while (m != null) {
                            if (id.contains(":")) {
                                if (Objects.equals(m.config.getId(), NamespacedKey.fromString(id))) break;
                            } else if (m.config.getId() != null) {
                                if (Objects.equals(m.config.getId().getKey(), id)) break;
                            }
                            m = m.previousMenu;
                        }
                        if (m != null) {
                            if (args.containsKey("commands"))
                                runIn((String) args.get("commands"), m, v.menu.loader, v);
                            m.reopen();
                        } else {
                            v.menu.viewer.closeInventory();
                        }
                    });
                })
        );
        commands.sub(new Command<ExecuteContext>("[BACK]")
                .aliases("[back]")
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> {
                    var m = Objects.requireNonNull(v.menu.previousMenu, "does not have a previous menu!");
                    if (args.containsKey("commands"))
                        runIn((String) args.get("commands"), m, v.menu.loader, v);
                    m.reopen();
                })
        );
        commands.sub(new Command<ExecuteContext>("[REFRESH]")
                .aliases("[refresh]")
                .executor((v, args) -> v.menu.refresh())
        );
        commands.sub(new Command<ExecuteContext>("[MESSAGE]")
                .aliases("[message]")
                .argument(new ArgumentComponents<>("msg"))
                .executor((v, args) -> {
                            Component msg = (Component) args.getOrDefault("msg", Component.empty());
                            v.menu.viewer.sendMessage(msg);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[ACTION_BAR]")
                .aliases("[action_bar]")
                .argument(new ArgumentComponents<>("msg"))
                .executor((v, args) -> {
                            Component msg = (Component) args.getOrDefault("msg", Component.empty());
                            v.menu.viewer.sendActionBar(msg);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[ACTION_BAR_ALL]")
                .aliases("[action_bar_all]")
                .argument(new ArgumentComponents<>("msg"))
                .executor((v, args) -> {
                            Component msg = (Component) args.getOrDefault("msg", Component.empty());
                            Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(msg));
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[TITLE]")
                .aliases("[title]")
                .argument(new ArgumentString<>("msg"))
                .argument(new ArgumentInt<>("fadeIn"))
                .argument(new ArgumentInt<>("stay"))
                .argument(new ArgumentInt<>("fadeOut"))

                .executor((v, args) -> {
                            //todo хочу ArgumentComponent а тут \n
                            String msg = (String) args.getOrThrow("msg", "Use [TITLE] <\"Title\\nSubTitle\"> <?fadeIn> <?stay> <?fadeOut>");
                            int fadeIn = (int) args.getOrDefault("fadeIn", 10);
                            int stay = (int) args.getOrDefault("stay", 70);
                            int fadeOut = (int) args.getOrDefault("fadeOut", 20);

                            String[] arr = msg.split("\\\\n", 2);
                            v.menu.viewer.showTitle(Title.title(
                                    MiniMessage.deserialize(arr[0]),
                                    arr.length == 2 ? MiniMessage.deserialize(arr[1]) : Component.empty(),
                                    Title.Times.of(
                                            Ticks.duration(fadeIn),
                                            Ticks.duration(stay),
                                            Ticks.duration(fadeOut)
                                    )
                            ));
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[TITLE_ALL]")
                .aliases("[title_all]")
                .argument(new ArgumentString<>("msg"))
                .argument(new ArgumentInt<>("fadeIn"))
                .argument(new ArgumentInt<>("stay"))
                .argument(new ArgumentInt<>("fadeOut"))
                .executor((v, args) -> {
                            //todo хочу ArgumentComponent а тут \n
                            String msg = (String) args.getOrThrow("msg", "Use [TITLE_ALL] <\"Title\\nSubTitle\"> <?fadeIn> <?stay> <?fadeOut>");
                            int fadeIn = (int) args.getOrDefault("fadeIn", 10);
                            int stay = (int) args.getOrDefault("stay", 70);
                            int fadeOut = (int) args.getOrDefault("fadeOut", 20);

                            String[] arr = msg.split("\\\\n", 2);
                            Title title = Title.title(
                                    MiniMessage.deserialize(arr[0]),
                                    arr.length == 2 ? MiniMessage.deserialize(arr[1]) : Component.empty(),
                                    Title.Times.of(
                                            Ticks.duration(fadeIn),
                                            Ticks.duration(stay),
                                            Ticks.duration(fadeOut)
                                    )
                            );
                            Bukkit.getOnlinePlayers().forEach(player -> player.showTitle(title));
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[BROADCAST]")
                .aliases("[broadcast]")
                .argument(new ArgumentComponents<>("msg"))
                .executor((v, args) -> {
                            Component msg = (Component) args.getOrDefault("msg", Component.empty());
                            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(msg));
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[SET_PARAM]")
                .aliases("[set_param]")
                .argument(new ArgumentString<>("param"))
                .argument(new ArgumentStrings<>("value"))
                .executor((v, args) -> {
                            String param = (String) args.getOrThrow("param", "Use [set_param] <param> <value>");
                            String value = (String) args.getOrThrow("value", "Use [set_param] <param> <value>");
                            v.menu.addArgument(param, value);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[IMPORT_PARAMS]")
                .aliases("[import_params]")
                .argument(new ArgumentString<>("item"))
                .executor((v, args) -> {
                            String param = (String) args.getOrThrow("item", "Use [import_params] <item>");
                            MenuItemBuilder builder = v.menu.config.findMenuItem(param, v.menu);
                            if (builder == null) {
                                throw new CommandError("No such item: '{}'. Command [import_params]", param);
                            }
                            v.menu.args.putAll(builder.getArgs());
                            v.menu.args.keySet().forEach(k -> v.menu.runtimePlaceholders.of(k, () -> v.menu.args.get(k)));
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[OPEN]")
                .aliases("[open]")
                .argument(new ArgumentString<>("menu"))
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> {
                            String menu = (String) args.getOrThrow("menu", "Use [open] <menu id>");
                            Menu m = v.menu.loader.create(menu, v.menu.viewer, v.menu);
                            if (args.containsKey("commands"))
                                runIn((String) args.get("commands"), m, v.menu.loader, v);
                            m.open();
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[BACK_OR_OPEN]")
                .aliases("[back_or_open]")
                .argument(new ArgumentString<>("menu"))
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> {
                    String menu = (String) args.getOrThrow("menu", "Use [back_or_open] <menu id>");
                    v.menu.sync(() -> {
                        if (v.menu.previousMenu != null) {
                            if (args.containsKey("commands"))
                                runIn((String) args.get("commands"), v.menu.previousMenu, v.menu.loader, v);
                            v.menu.previousMenu.reopen();
                        } else {
                            Menu m = v.menu.loader.create(menu, v.menu.viewer, v.menu);
                            if (args.containsKey("commands"))
                                runIn((String) args.get("commands"), m, v.menu.loader, v);
                            m.open();
                        }
                    });
                })
        );
        commands.sub(new Command<ExecuteContext>("[run_rand]")
                .aliases("[run_rand]")
                .executor((v, args) -> {
                            Commands commands = v.menu.config.getCommandList().getRandom();
                            if (commands == null) {
                                throw new CommandError("commands-list не определён в конфиге!");
                            }
                            commands.run(v, v.menu);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[RUN]")
                .aliases("[run]")
                .argument(new ArgumentString<>("name"))
                .executor((v, args) -> {
                            String name = (String) args.getOrThrow("name", "Use [run] <name>");
                            Commands commands = v.menu.config.getCommandList().getByName(name);
                            if (commands == null) {
                                throw new CommandError("В commands-list не пределён набор команд {}", name);
                            }
                            commands.run(v, v.menu);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[REOPEN]")
                .aliases("[reopen]")
                .executor((v, args) -> v.menu.reopen()
                )
        );
        commands.sub(new Command<ExecuteContext>("[ANIMATION_FORCE_END]")
                .aliases("[animation_force_end]")
                .executor((v, args) -> {
                            if (v.menu.animator != null) {
                                v.menu.animator.forceEnd(v.menu.layers.getAnimationLayer(), v.menu);
                            }
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[ANIMATION_TICK]")
                .aliases("[animation_tick]")
                .executor((v, args) -> {
                            if (v.menu.animator != null && !v.menu.animator.isEnd()) {
                                v.menu.animator.tick(v.menu.layers.getAnimationLayer(), v.menu);
                            }
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[FLUSH]")
                .aliases("[flush]")
                .executor((v, args) -> {
                            v.menu.flush();
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[CONNECT]")
                .aliases("[connect]")
                .argument(new ArgumentString<>("server"))
                .executor((v, args) -> {
                            String server = (String) args.getOrThrow("server", "Use [connect] <server>");
                            BungeeCordMessageSender.connectPlayerToServer(v.menu.viewer, server, v.menu.loader.getPlugin());
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[SET_ANIMATION]")
                .aliases("[set_animation]")
                .argument(new ArgumentString<>("animation"))
                .executor((v, args) -> {
                            String animation = (String) args.getOrThrow("animation", "Use: [set_animation] <animation>");
                            Animator.AnimatorContext ctx = v.menu.config.getAnimations().get(animation);
                            if (ctx == null) {
                                throw new CommandError("Неизвестная анимация {}", animation);
                            }
                            if (v.menu.animator != null) {
                                v.menu.animator.forceEnd(v.menu.layers.getAnimationLayer(), v.menu);
                            }
                            v.menu.animator = ctx.createAnimator();

                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[GIVEMONEY]")
                .aliases("[givemoney]")
                .argument(new ArgumentDouble<>("count"))
                .executor((v, args) -> {
                            Double count = (Double) args.getOrThrow("count", "Use: [givemoney] <count>");
                            if (!VaultHook.get().isAvailable()) {
                                throw new CommandError("Economy not defined");
                            }
                            VaultHook.get().depositPlayer(v.menu.viewer, count);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[TAKEMONEY]")
                .aliases("[takemoney]")
                .argument(new ArgumentDouble<>("count"))
                .executor((v, args) -> {
                            Double count = (Double) args.getOrThrow("count", "Use: [takemoney] <count>");
                            if (!VaultHook.get().isAvailable()) {
                                throw new CommandError("Economy not defined");
                            }
                            VaultHook.get().withdrawPlayer(v.menu.viewer, count);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[DELAY]")
                .aliases("[delay]")
                .argument(new ArgumentInt<>("delay"))
                .argument(new ArgumentCommand<>("cmd", Menu::getCommands))
                .executor((v, args) -> {
                            int delay = (int) args.getOrThrow("delay", "Use: [delay] <delay> <command>");
                            ArgumentCommand.RunnableCommand<ExecuteContext> cmd = (ArgumentCommand.RunnableCommand<ExecuteContext>) args.getOrThrow("cmd", "Use: [delay] <delay> <command>");
                            v.menu.sync(() -> cmd.run(v, s -> s), delay);
                        }
                )
        );
        commands.sub(new Command<ExecuteContext>("[COPY_FROM_PREVIOUS_MENU]")
                        .aliases("[copy_from_previous_menu]")
                        .argument(new ArgumentString<>("src"))
                        .argument(new ArgumentString<>("dest"))
                        .executor((v, args) -> {
                                    int[] src = AnimationUtil.readSlots((String) args.getOrThrow("src", "Use: [copy_from_previous_menu] <src> <dest>"));
                                    int[] dest = AnimationUtil.readSlots((String) args.getOrThrow("dest", "Use: [copy_from_previous_menu] <src> <dest>"));
                                    if (v.menu.previousMenu == null) {
                                        throw new CommandError("Failed to execute [copy_from_previous_menu] because previousMenu is null!");
                                    }

                                    //todo
                                    //  int srcIndex = 0;
                                    //  for (int toIndex : dest) {
                                    //      int fromIndex = src[srcIndex];
//
                                    //      if (fromIndex < 0 || fromIndex >= v.menu.matrix.length || toIndex < 0 || toIndex >= v.menu.matrix.length) {
                                    //          throw new IndexOutOfBoundsException("Индексы 'from' или 'to' выходят за пределы меню.");
                                    //      }
//
                                    //      v.menu.animationMask[toIndex] = v.menu.previousMenu.findItemInSlot(fromIndex);
                                    //      if (srcIndex < src.length - 1) {
                                    //          srcIndex++;
                                    //      }
                                    //  }

                                }
                        )
        );
        commands.sub(new Command<ExecuteContext>("[REBUILD_SLOTS]")
                .aliases("[rebuild_slots]")
                .argument(new ArgumentString<>("src"))
                .executor((v, args) -> {
                    int[] src = AnimationUtil.readSlots((String) args.getOrThrow("src", "Use: [rebuild_slots] <slots>"));
                    v.menu.rebuildItemsInSlots(src);
                })

        );
        commands.sub(new Command<ExecuteContext>("[update_slots]")
                .aliases("[UPDATE_SLOTS]")
                .argument(new ArgumentString<>("src"))
                .executor((v, args) -> {
                    int[] src = AnimationUtil.readSlots((String) args.getOrThrow("src", "Use: [update_slots] <slots>"));
                    for (int slot : src) {
                        MenuItem item = v.menu.findItemInSlot(slot);
                        if (item != null) {
                            item.setDirty(true);
                        }
                    }
                })

        );
        commands.sub(new Command<ExecuteContext>("[die_slots]")
                .aliases("[DIE_SLOTS]")
                .argument(new ArgumentString<>("src"))
                .executor((v, args) -> {
                    int[] src = AnimationUtil.readSlots((String) args.getOrThrow("src", "Use: [die_slots] <slots>"));
                    for (int slot : src) {
                        MenuItem item = v.menu.findItemInSlot(slot);
                        if (item != null) {
                            item.die();
                        }
                    }
                })

        );
        commands.sub(new Command<ExecuteContext>("[set_title]")
                .aliases("[SET_TITLE]")
                .argument(new ArgumentString<>("title"))
                .executor((v, args) -> {
                    String title = (String) args.getOrThrow("title", "Use: [set_title] <title>");
                    v.menu.setTitle(title);
                })

        );

        commands.sub(new Command<ExecuteContext>("[rebuild]")
                .executor((v, args) -> {
                    if (v.item != null) {
                        v.item.setDirty(true); //todo надо наверное сбросить локальные плейсы
                    }
                })

        );
        commands.sub(new Command<ExecuteContext>("[update]")
                .executor((v, args) -> {
                    if (v.item != null) {
                        v.item.setDirty(true);
                    }
                })
        );
        commands.sub(new Command<ExecuteContext>("[die]")
                .executor((v, args) -> {
                    if (v.item != null) {
                        v.item.die();
                    }
                })
        );
        commands.sub(new Command<ExecuteContext>("[set_local]")
                .argument(new ArgumentString<>("param"))
                .argument(new ArgumentString<>("value"))
                .executor((v, args) -> {
                    String param = (String) args.getOrThrow("param", "Use: [set_local] <param> <value>");
                    String value = (String) args.getOrThrow("value", "Use: [set_local] <param> <value>");
                    if (v.item != null) {
                        v.item.setPlaceholder(param, () -> value);
                    }
                })

        );


        Command<ExecuteContext> layerCommands = new Command<ExecuteContext>("[layer]")
                .aliases("[LAYER]");
        for (int i = 0; i < 16; i++) { // костыль чтобы можно было писать [layer] 0 <под команда>
            final int layerIndex = i;
            layerCommands.sub(
                    new Command<ExecuteContext>(Integer.toString(layerIndex))
                            .sub(new Command<ExecuteContext>("[move]")
                                    .argument(new ArgumentInt<>("layer2"))
                                    .argument(new ArgumentString<>("from"))
                                    .argument(new ArgumentString<>("to"))
                                    .executor((v, args) -> {
                                        int layer2 = (int) args.getOrThrow("layer2", "Use: [layer] <layer> [move] <slots-from> <slots-to>");
                                        int[] from = AnimationUtil.readSlots((String) args.getOrThrow("from", "Use: [layer] <layer> [move] <slots-from> <slots-to>"));
                                        int[] to = AnimationUtil.readSlots((String) args.getOrThrow("to", "Use: [layer] <layer> [move] <slots-from> <slots-to>"));
                                        MenuItem[] src = v.menu.layers.getMatrix(layerIndex);
                                        MenuItem[] dest = v.menu.layers.getMatrix(layer2);
                                        for (int idx = 0; idx < from.length; idx++) {
                                            int fromIndex = from[idx];
                                            int toIndex = to[idx];

                                            if (fromIndex < 0 || fromIndex >= src.length || toIndex < 0 || toIndex >= dest.length) {
                                                throw new IndexOutOfBoundsException("Индексы 'from' или 'to' выходят за пределы меню.");
                                            }
                                            dest[toIndex] = src[fromIndex];
                                            src[fromIndex] = null;
                                        }
                                    })
                            )
                            .sub(new Command<ExecuteContext>("[copy]")
                                    .argument(new ArgumentInt<>("layer2"))
                                    .argument(new ArgumentString<>("from"))
                                    .argument(new ArgumentString<>("to"))
                                    .executor((v, args) -> {
                                        int layer2 = (int) args.getOrThrow("layer2", "Use: [layer] <layer> [copy] <slots-from> <slots-to>");
                                        int[] from = AnimationUtil.readSlots((String) args.getOrThrow("from", "Use: [layer] <layer> [copy] <slots-from> <slots-to>"));
                                        int[] to = AnimationUtil.readSlots((String) args.getOrThrow("to", "Use: [layer] <layer> [copy] <slots-from> <slots-to>"));
                                        MenuItem[] src = v.menu.layers.getMatrix(layerIndex);
                                        MenuItem[] dest = v.menu.layers.getMatrix(layer2);
                                        for (int idx = 0; idx < from.length; idx++) {
                                            int fromIndex = from[idx];
                                            int toIndex = to[idx];

                                            if (fromIndex < 0 || fromIndex >= src.length || toIndex < 0 || toIndex >= dest.length) {
                                                throw new CommandError("Индексы 'from' или 'to' выходят за пределы меню.");
                                            }
                                            dest[toIndex] = src[fromIndex];
                                        }
                                    })
                            ).sub(new Command<ExecuteContext>("[set]")
                                    .argument(new ArgumentString<>("item"))
                                    .argument(new ArgumentString<>("slots"))
                                    .executor((v, args) -> {
                                        String itemID = (String) args.getOrThrow("item", "Use: [layer] <layer> [set] <item> <slots>");
                                        int[] slots = AnimationUtil.readSlots((String) args.getOrThrow("slots", "Use: [layer] <layer> [set] <item> <slots>"));
                                        MenuItem[] src = v.menu.layers.getMatrix(layerIndex);
                                        MenuItemBuilder builder = v.menu.config.findMenuItem(itemID, v.menu);
                                        MenuItem item;
                                        if (builder != null) {
                                            item = builder.build(v.menu);
                                        } else {
                                            item = MenuItem.ofMaterial(itemID);
                                        }
                                        for (int slot : slots) {
                                            if (slot < 0 || slot >= src.length) {
                                                throw new CommandError("слот {} за пределами меню!", slot);
                                            }
                                            src[slot] = item;
                                        }
                                    })
                            )
                            .sub(new Command<ExecuteContext>("[clear]")
                                    .executor((v, args) -> {
                                        Arrays.fill(v.menu.layers.getMatrix(layerIndex), null);
                                    })
                            )
            );

        }
        commands.sub(layerCommands);
    }

}
