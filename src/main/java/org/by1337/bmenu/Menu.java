package org.by1337.bmenu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.StringReader;
import org.by1337.blib.command.argument.*;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.NBTParser;
import org.by1337.blib.nbt.impl.ListNBT;
import org.by1337.blib.nbt.impl.StringNBT;
import org.by1337.blib.text.MessageFormatter;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.animation.util.AnimationUtil;
import org.by1337.bmenu.click.MenuClickType;
import org.by1337.bmenu.hook.ItemStackCreator;
import org.by1337.bmenu.hook.VaultHook;
import org.by1337.bmenu.network.BungeeCordMessageSender;
import org.by1337.bmenu.requirement.CommandRequirements;
import org.by1337.bmenu.util.math.MathReplacer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.*;

public abstract class Menu extends Placeholder implements InventoryHolder {
    private static final Command<Menu> commands;
    protected final MenuConfig config;
    protected final MenuLoader loader;
    protected final Player viewer;
    protected Inventory inventory;
    protected final MenuItem[] matrix;
    protected final MenuItem[] animationMask;
    protected final Map<String, String> args;
    @Nullable
    protected final Menu previousMenu;
    protected BukkitTask ticker;
    protected Animator animator;
    protected @Nullable MenuItem lastClickedItem;
    protected long lastClickTime;

    public Menu(MenuConfig config, Player viewer, @Nullable Menu previousMenu) {
        this.config = config;
        loader = config.getLoader();
        this.viewer = viewer;
        matrix = new MenuItem[config.getSize()];
        animationMask = new MenuItem[config.getSize()];
        args = new HashMap<>(config.getArgs());
        this.previousMenu = previousMenu;
        registerPlaceholders(RandomPlaceholders.getInstance());
        args.keySet().forEach(k -> registerPlaceholder("${" + k + "}", () -> args.get(k)));
        registerPlaceholder("{has_back_menu}", () -> String.valueOf(previousMenu != null));
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
        if (inventory == null) {
            createInventory(config.getSize(), loader.getMessage().componentBuilder(replace(config.getTitle())), config.getInvType());
        }
        sync(() -> {
            inventory.clear();
            if (!Objects.equals(viewer.getOpenInventory().getTopInventory(), inventory)) {
                viewer.openInventory(inventory);
            }
            // Отключаем автоматическую синхронизацию инвентаря с клиентом,
            // так как сервер может периодически отправлять обновления,
            // что может нарушить отображение анимаций.
            BLib.getApi().getInventoryUtil().disableAutoFlush(viewer);

            onEvent(isReopen ? MenuEvents.ON_REOPEN : MenuEvents.ON_OPEN);
            if (animator == null && config.getAnimation() != null) {
                animator = config.getAnimation().createAnimator();
            }
            if (!Objects.equals(viewer.getOpenInventory().getTopInventory(), inventory)) {
                return;
            }
            Arrays.fill(matrix, null);
            Arrays.fill(animationMask, null);
            if (animator != null && !animator.isEnd()) {
                animator.tick(animationMask, this);
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
            animator.tick(animationMask, this);
            inventory.clear();
        }
        doItemTick(matrix);
        doItemTick(animationMask);
        flush();
    }

    private void doItemTick(MenuItem[] matrix) {
        Map<MenuItem, MenuItem> cache = new IdentityHashMap<>();
        for (int i = 0; i < matrix.length; i++) {
            MenuItem item = matrix[i];
            if (item != null && item.isTicking() && item.getBuilder() != null) {
                item.doTick();
                if (item.shouldBeRebuild()) {
                    matrix[i] = cache.computeIfAbsent(item, k -> k.getBuilder().get());
                }
            }
        }
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

    public void refresh() {
        Map<MenuItem, MenuItem> cache = new IdentityHashMap<>();
        for (int i = 0; i < animationMask.length; i++) {
            MenuItem item = animationMask[i];
            if (item == null || item.getBuilder() == null) continue;
            animationMask[i] = cache.computeIfAbsent(item, k -> k.getBuilder().get());
        }
        generate0();
    }

    protected abstract void generate();

    protected void generate0() {
        Arrays.fill(matrix, null);
        config.generate(this);
        generate();
        onEvent(MenuEvents.ON_REFRESH);
        sendFakeTitle(config.getTitle());
        inventory.clear();
        flush();
    }

    public void close() {
        viewer.closeInventory();
    }

    protected void flush() {
        setMatrix(matrix);
        setMatrix(animationMask);
        BLib.getApi().getInventoryUtil().flushInv(viewer);
    }

    protected void setMatrix(MenuItem[] mask) {
        for (int i = 0; i < mask.length; i++) {
            MenuItem item = mask[i];
            if (item != null) {
                inventory.setItem(i, item.getItemStack());
            }
        }
    }

    public void setItems(List<MenuItem> item) {
        for (MenuItem menuItem : item) {
            setItem(menuItem, matrix);
        }
    }

    public void setItem(MenuItem item, MenuItem[] matrix) {
        for (int slot : item.getSlots()) {
            if (slot == -1) continue;
            if (slot < 0 || matrix.length < slot) {
                loader.getLogger().error("Slot {} is out of bounds! Menu: {}", slot, config.getId());
            } else {
                matrix[slot] = item;
            }
        }
    }

    public void setItem(MenuItem item) {
        setItem(item, matrix);
    }

    protected void createInventory(int size, Component title, InventoryType type) {
        if (type == InventoryType.CHEST) {
            inventory = Bukkit.createInventory(this, size, title);
        } else {
            inventory = Bukkit.createInventory(this, type, title);
        }
    }

    @Override
    public String replace(String string) {
        try {
            return MathReplacer.replace(loader.getMessage().setPlaceholders(viewer, super.replace(string)));
        } catch (ParseException e) {
            loader.getLogger().error("Failed to parse math", e);
            return string;
        }
    }

    protected abstract boolean runCommand(String cmd) throws CommandException;

    public void runCommands(List<String> commands) {
        for (String command : commands) {
            String replaced = replace(command);
            try {
                if (!runCommand(replaced)) {
                    Menu.commands.process(this, new StringReader(replaced));
                }
            } catch (CommandException e) {
                loader.getLogger().error("Failed to run command: {}", replaced, e);
            }
        }
    }

    public void onClose(InventoryCloseEvent event) {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        // Восстанавливаем автоматическую синхронизацию инвентаря с клиентом,
        // так как пользователь закрыл кастомное меню, и ответственность за обновление
        // инвентаря теперь возвращается серверу.
        BLib.getApi().getInventoryUtil().enableAutoFlush(viewer);

        onEvent(MenuEvents.ON_CLOSE);
    }

    public void onClick(InventoryDragEvent e) {
        lastClickTime = System.currentTimeMillis();
    }

    public void onClick(InventoryClickEvent e) {
        lastClickTime = System.currentTimeMillis();
        if (e.getCurrentItem() == null) {
            return;
        }
        if (!Objects.equals(inventory, e.getClickedInventory())) return;
        MenuItem item = findItemInSlot(e.getSlot());
        if (item == null) return;
        lastClickedItem = item;
        MenuClickType type = MenuClickType.getClickType(e);
        item.doClick(this, viewer, type);
    }

    @Nullable
    protected MenuItem findItemInSlot(int slot) {
        MenuItem item = findItemInSlot(slot, animationMask);
        return item == null ? findItemInSlot(slot, matrix) : item;
    }

    protected MenuItem findItemInSlot(int slot, MenuItem[] matrix) {
        if (slot >= matrix.length || slot < 0) return null;
        return matrix[slot];
    }

    protected void sync(Runnable runnable) {
        sync(runnable, 0);
    }

    protected void sync(Runnable runnable, int delay) {
        if (!Bukkit.isPrimaryThread() || delay != 0)
            Bukkit.getScheduler().runTaskLater(loader.getPlugin(), runnable, delay);
        else runnable.run();
    }

    protected void sendFakeTitle(String title) {
        BLib.getApi().getInventoryUtil().sendFakeTitle(inventory, loader.getMessage().componentBuilder(replace(title)));
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
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public MenuItem[] getMatrix() {
        return matrix;
    }

    public MenuItem[] getAnimationMask() {
        return animationMask;
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
        String name = "${" + key + "}";
        placeholders.remove(name);
        registerPlaceholder(name, () -> args.get(key));
    }

    /**
     * Определяет, поддерживает ли меню горячую перезагрузку.
     * Если возвращает {@code true}, то при горячей перезагрузке будет создан новый экземпляр меню,
     * а также новый экземпляр {@link Menu#previousMenu}.
     *
     * @return {@code true}, если меню поддерживает горячую перезагрузку, иначе {@code false}.
     */
    @ApiStatus.Experimental
    public boolean isSupportsHotReload() {
        return false;
    }

    /**
     * Вызывается при горячей перезагрузке меню. Позволяет перенести данные из старого экземпляра в новый.
     * Этот метод вызывается только если {@link #isSupportsHotReload()} возвращает {@code true}.
     *
     * @param oldMenu старый экземпляр меню, из которого можно перенести данные в новый.
     */
    @ApiStatus.Experimental
    public void onHotReload(@NotNull Menu oldMenu) {
        args.putAll(oldMenu.args);
        for (String param : args.keySet()) {
            registerPlaceholder("${" + param + "}", () -> args.get(param));
        }
    }


    @Override
    public String toString() {
        return "Menu{" +
                "loader=" + loader +
                ", config=" + config +
                ", viewer=" + viewer +
                '}';
    }

    private static void runIn(String rawNBT, Menu menu, MenuLoader loader) {
        if (rawNBT != null && !rawNBT.isBlank()) {
            try {
                ListNBT listNBT = (ListNBT) NBTParser.parseList(rawNBT);
                List<String> list = new ArrayList<>();
                for (NBT nbt : listNBT) {
                    if (nbt instanceof StringNBT stringNBT) {
                        list.add(stringNBT.getValue());
                    } else {
                        throw new IllegalArgumentException(String.format("Input: '%s' expected StringNBT", nbt));
                    }
                }
                menu.runCommands(list);
            } catch (Throwable t) {
                loader.getLogger().error("Failed to parse commands {}", rawNBT, t);
            }
        }
    }

    static {
        commands = new Command<>("root");
        commands.addSubCommand(new Command<Menu>("[CONSOLE]")
                .aliases("[console]")
                .argument(new ArgumentStrings<>("cmd"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("cmd");
                            v.sync(() -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd));
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[PLAYER]")
                .aliases("[player]")
                .argument(new ArgumentStrings<>("cmd"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("cmd");
                            v.sync(() -> v.viewer.performCommand(cmd));
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[CHAT]")
                .aliases("[chat]")
                .argument(new ArgumentStrings<>("cmd"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("cmd");
                            v.sync(() -> v.viewer.chat(cmd));
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[SOUND]")
                .aliases("[sound]")
                .argument(new ArgumentSound<>("sound"))
                .argument(new ArgumentFloat<>("volume"))
                .argument(new ArgumentFloat<>("pitch"))
                .executor((v, args) -> {
                            float volume = (float) args.getOrDefault("volume", 1F);
                            float pitch = (float) args.getOrDefault("pitch", 1F);
                            Sound sound = (Sound) args.getOrThrow("sound");
                            v.loader.getMessage().sendSound(v.viewer, sound, volume, pitch);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[CLOSE]")
                .aliases("[close]")
                .executor((v, args) -> v.sync(v.viewer::closeInventory))
        );
        commands.addSubCommand(new Command<Menu>("[BACK_OR_CLOSE]")
                .aliases("[back_or_close]")
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> v.sync(() -> {
                    if (v.previousMenu != null) {
                        if (args.containsKey("commands"))
                            runIn((String) args.get("commands"), v.previousMenu, v.loader);
                        v.previousMenu.reopen();
                    } else {
                        v.viewer.closeInventory();
                    }
                }))
        );
        commands.addSubCommand(new Command<Menu>("[BACK_TO_OR_CLOSE]")
                .aliases("[back_to_or_close]")
                .argument(new ArgumentString<>("id"))
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> {
                    String id = (String) args.getOrThrow("id", "Use: [BACK_TO_OR_CLOSE] <id>");
                    v.sync(() -> {
                        Menu m = v.previousMenu;
                        while (m != null) {
                            if (id.contains(":")) {
                                if (Objects.equals(m.config.getId(), new SpacedNameKey(id))) break;
                            } else if (m.config.getId() != null) {
                                if (Objects.equals(m.config.getId().getName().getName(), id)) break;
                            }
                            m = m.previousMenu;
                        }
                        if (m != null) {
                            if (args.containsKey("commands"))
                                runIn((String) args.get("commands"), m, v.loader);
                            m.reopen();
                        } else {
                            v.viewer.closeInventory();
                        }
                    });
                })
        );
        commands.addSubCommand(new Command<Menu>("[BACK]")
                .aliases("[back]")
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> {
                    var m = Objects.requireNonNull(v.previousMenu, "does not have a previous menu!");
                    if (args.containsKey("commands"))
                        runIn((String) args.get("commands"), m, v.loader);
                    m.reopen();
                })
        );
        commands.addSubCommand(new Command<Menu>("[REFRESH]")
                .aliases("[refresh]")
                .executor((v, args) -> v.refresh())
        );
        commands.addSubCommand(new Command<Menu>("[MESSAGE]")
                .aliases("[message]")
                .argument(new ArgumentStrings<>("msg"))
                .executor((v, args) -> {
                            String msg = (String) args.getOrThrow("msg", "Use [MESSAGE] <msg>");
                            v.loader.getMessage().sendMsg(v.viewer, msg);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[ACTION_BAR]")
                .aliases("[action_bar]")
                .argument(new ArgumentStrings<>("msg"))
                .executor((v, args) -> {
                            String msg = (String) args.getOrThrow("msg", "Use [ACTION_BAR] <msg>");
                            v.loader.getMessage().sendActionBar(v.viewer, msg);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[ACTION_BAR_ALL]")
                .aliases("[action_bar_all]")
                .argument(new ArgumentStrings<>("msg"))
                .executor((v, args) -> {
                            String msg = (String) args.getOrThrow("msg", "Use [ACTION_BAR_ALL] <msg>");
                            v.loader.getMessage().sendAllActionBar(msg);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[TITLE]")
                .aliases("[title]")
                .argument(new ArgumentString<>("msg"))
                .argument(new ArgumentInteger<>("fadeIn"))
                .argument(new ArgumentInteger<>("stay"))
                .argument(new ArgumentInteger<>("fadeOut"))

                .executor((v, args) -> {
                            String msg = (String) args.getOrThrow("msg", "Use [TITLE] <\"Title\\nSubTitle\"> <?fadeIn> <?stay> <?fadeOut>");
                            int fadeIn = (int) args.getOrDefault("fadeIn", 10);
                            int stay = (int) args.getOrDefault("stay", 70);
                            int fadeOut = (int) args.getOrDefault("fadeOut", 20);

                            String[] arr = msg.split("\\\\n", 2);
                            v.loader.getMessage().sendTitle(v.viewer, arr[0], arr.length == 2 ? arr[1] : "", fadeIn, stay, fadeOut);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[TITLE_ALL]")
                .aliases("[title_all]")
                .argument(new ArgumentString<>("msg"))
                .argument(new ArgumentInteger<>("fadeIn"))
                .argument(new ArgumentInteger<>("stay"))
                .argument(new ArgumentInteger<>("fadeOut"))
                .executor((v, args) -> {
                            String msg = (String) args.getOrThrow("msg", "Use [TITLE_ALL] <\"Title\\nSubTitle\"> <?fadeIn> <?stay> <?fadeOut>");
                            int fadeIn = (int) args.getOrDefault("fadeIn", 10);
                            int stay = (int) args.getOrDefault("stay", 70);
                            int fadeOut = (int) args.getOrDefault("fadeOut", 20);

                            String[] arr = msg.split("\\\\n", 2);
                            Bukkit.getOnlinePlayers().forEach(player -> v.loader.getMessage().sendTitle(player, arr[0], arr.length == 2 ? arr[1] : "", fadeIn, stay, fadeOut));
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[BROADCAST]")
                .aliases("[broadcast]")
                .argument(new ArgumentStrings<>("msg"))
                .executor((v, args) -> {
                            String msg = (String) args.getOrThrow("msg", "Use [BROADCAST] <msg>");
                            v.loader.getMessage().sendAllMsg(msg);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[SET_PARAM]")
                .aliases("[set_param]")
                .argument(new ArgumentString<>("param"))
                .argument(new ArgumentStrings<>("value"))
                .executor((v, args) -> {
                            String param = (String) args.getOrThrow("param", "Use [SET_PARAM] <param> <value>");
                            String value = (String) args.getOrThrow("value", "Use [SET_PARAM] <param> <value>");
                            if (v.args.put(param, value) == null) {
                                v.registerPlaceholder("${" + param + "}", () -> v.args.get(param));
                            }
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[IMPORT_PARAMS]")
                .aliases("[import_params]")
                .argument(new ArgumentString<>("item"))
                .executor((v, args) -> {
                            String param = (String) args.getOrThrow("item", "Use [IMPORT_PARAMS] <item>");
                            MenuItemBuilder builder = v.config.findMenuItem(param, v);
                            if (builder == null) {
                                throw new CommandException("No such item: '{}'. Command [IMPORT_PARAMS]", param);
                            }
                            v.args.putAll(builder.getArgs());
                            v.args.keySet().forEach(k -> v.registerPlaceholder("${" + k + "}", () -> v.args.get(k)));
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[OPEN]")
                .aliases("[open]")
                .argument(new ArgumentString<>("menu"))
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> {
                            String menu = (String) args.getOrThrow("menu", "Use [OPEN_MENU] <menu id>");
                            MenuConfig settings = v.loader.findMenu(menu);
                            if (settings == null) {
                                throw new CommandException("Unknown menu %s", menu);
                            }
                            Menu m = v.loader.findAndCreate(settings, v.viewer, v);
                            if (args.containsKey("commands"))
                                runIn((String) args.get("commands"), m, v.loader);
                            m.open();
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[BACK_OR_OPEN]")
                .aliases("[back_or_open]")
                .argument(new ArgumentString<>("menu"))
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> {
                    String menu = (String) args.getOrThrow("menu", "Use [BACK_OR_OPEN] <menu id>");
                    MenuConfig settings = v.loader.findMenu(menu);
                    if (settings == null) {
                        throw new CommandException("Unknown menu %s", menu);
                    }
                    v.sync(() -> {
                        if (v.previousMenu != null) {
                            if (args.containsKey("commands"))
                                runIn((String) args.get("commands"), v.previousMenu, v.loader);
                            v.previousMenu.reopen();
                        } else {
                            Menu m = v.loader.findAndCreate(settings, v.viewer, v);
                            if (args.containsKey("commands"))
                                runIn((String) args.get("commands"), m, v.loader);
                            m.open();
                        }
                    });
                })
        );
        commands.addSubCommand(new Command<Menu>("[RUN_RAND]")
                .aliases("[run_rand]")
                .executor((v, args) -> {
                            List<String> commands = v.config.getCommandList().getRandom();
                            if (commands == null) {
                                throw new CommandException("commands-list не определён в конфиге!");
                            }
                            v.runCommands(commands);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[RUN]")
                .aliases("[run]")
                .argument(new ArgumentString<>("name"))
                .executor((v, args) -> {
                            String name = (String) args.getOrThrow("name", "Use [RUN] <name>");
                            List<String> commands = v.config.getCommandList().getByName(name);
                            if (commands == null) {
                                throw new CommandException("В commands-list не пределён набор команд {}", name);
                            }
                            v.runCommands(commands);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[REOPEN]")
                .aliases("[reopen]")
                .executor((v, args) -> {
                            v.reopen();
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[ANIMATION_FORCE_END]")
                .aliases("[animation_force_end]")
                .executor((v, args) -> {
                            if (v.animator != null) {
                                v.animator.forceEnd(v.animationMask, v);
                            }
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[ANIMATION_TICK]")
                .aliases("[animation_tick]")
                .executor((v, args) -> {
                            if (v.animator != null && !v.animator.isEnd()) {
                                v.animator.tick(v.animationMask, v);
                            }
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[FLUSH]")
                .aliases("[flush]")
                .executor((v, args) -> {
                            v.inventory.clear();
                            v.flush();
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[CONNECT]")
                .aliases("[connect]")
                .argument(new ArgumentString<>("server"))
                .executor((v, args) -> {
                            String server = (String) args.getOrThrow("server", "Use [CONNECT] <server>");
                            BungeeCordMessageSender.connectPlayerToServer(v.viewer, server, v.loader.getPlugin());
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[SET_ANIMATION]")
                .aliases("[set_animation]")
                .argument(new ArgumentString<>("animation"))
                .executor((v, args) -> {
                            String animation = (String) args.getOrThrow("animation", "Use: [SET_ANIMATION] <animation>");
                            Animator.AnimatorContext ctx = v.config.getAnimations().get(animation);
                            if (ctx == null) {
                                throw new CommandException("Неизвестная анимация {}", animation);
                            }
                            if (v.animator != null) {
                                v.animator.forceEnd(v.animationMask, v);
                            }
                            v.animator = ctx.createAnimator();
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[GIVEMONEY]")
                .aliases("[givemoney]")
                .argument(new ArgumentFormattedDouble<>("count"))
                .executor((v, args) -> {
                            Double count = (Double) args.getOrThrow("count", "Use: [GIVEMONEY] <count>");
                            if (!VaultHook.get().isAvailable()) {
                                throw new CommandException("Economy not defined");
                            }
                            VaultHook.get().depositPlayer(v.viewer, count);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[TAKEMONEY]")
                .aliases("[takemoney]")
                .argument(new ArgumentFormattedDouble<>("count"))
                .executor((v, args) -> {
                            Double count = (Double) args.getOrThrow("count", "Use: [GIVEMONEY] <count>");
                            if (!VaultHook.get().isAvailable()) {
                                throw new CommandException("Economy not defined");
                            }
                            VaultHook.get().withdrawPlayer(v.viewer, count);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[DELAY]")
                .aliases("[delay]")
                .argument(new ArgumentInteger<>("delay"))
                .argument(new ArgumentStrings<>("cmd"))
                .executor((v, args) -> {
                            int delay = (int) args.getOrThrow("delay", "Use: [DELAY] <delay> <command>");
                            String cmd = (String) args.getOrThrow("cmd", "Use: [DELAY] <delay> <command>");
                            v.sync(() -> v.runCommands(List.of(cmd)), delay);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[SET_ITEM]")
                .aliases("[set_item]")
                .argument(new ArgumentString<>("slots"))
                .argument(new ArgumentString<>("item"))
                .executor((v, args) -> {
                            int[] slots = AnimationUtil.readSlots((String) args.getOrThrow("slots", "Use: [SET_ITEM] <slots> <item>"));
                            String item = (String) args.getOrThrow("item", "Use: [SET_ITEM] <slot> <item>");
                            MenuItemBuilder builder = v.getConfig().findMenuItem(v.replace(item), v);
                            MenuItem menuItem;
                            if (builder == null) {
                                menuItem = new MenuItem(slots, ItemStackCreator.getItem(v.replace(item)));
                            } else {
                                menuItem = builder.build(v);
                            }
                            if (menuItem != null) {
                                menuItem.setSlots(slots);
                                v.setItem(menuItem, v.animationMask);
                            }
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[COPY_FROM_PREVIOUS_MENU]")
                .aliases("[copy_from_previous_menu]")
                .argument(new ArgumentString<>("src"))
                .argument(new ArgumentString<>("dest"))
                .executor((v, args) -> {
                            int[] src = AnimationUtil.readSlots((String) args.getOrThrow("src", "Use: [COPY_FROM_PREVIOUS_MENU] <src> <dest>"));
                            int[] dest = AnimationUtil.readSlots((String) args.getOrThrow("dest", "Use: [COPY_FROM_PREVIOUS_MENU] <src> <dest>"));
                            if (v.previousMenu == null) {
                                throw new CommandException("Failed to execute [COPY_FROM_PREVIOUS_MENU] because previousMenu is null!");
                            }

                            int srcIndex = 0;
                            for (int toIndex : dest) {
                                int fromIndex = src[srcIndex];

                                if (fromIndex < 0 || fromIndex >= v.matrix.length || toIndex < 0 || toIndex >= v.matrix.length) {
                                    throw new IndexOutOfBoundsException("Индексы 'from' или 'to' выходят за пределы меню.");
                                }

                                v.animationMask[toIndex] = v.previousMenu.findItemInSlot(fromIndex);
                                if (srcIndex < src.length - 1) {
                                    srcIndex++;
                                }
                            }

                        }
                )
        );
    }
}
