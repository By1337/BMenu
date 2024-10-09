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
import org.by1337.blib.command.argument.ArgumentEnumValue;
import org.by1337.blib.command.argument.ArgumentFloat;
import org.by1337.blib.command.argument.ArgumentString;
import org.by1337.blib.command.argument.ArgumentStrings;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.NBTParser;
import org.by1337.blib.nbt.impl.ListNBT;
import org.by1337.blib.nbt.impl.StringNBT;
import org.by1337.blib.util.SpacedNameKey;
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.click.MenuClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
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
    protected BukkitTask animationTask;
    protected Animator animator;

    public Menu(MenuConfig config, Player viewer, @Nullable Menu previousMenu) {
        this.config = config;
        loader = config.getLoader();
        this.viewer = viewer;
        matrix = new MenuItem[config.getSize()];
        animationMask = new MenuItem[config.getSize()];
        args = new HashMap<>(config.getArgs());
        this.previousMenu = previousMenu;
        args.keySet().forEach(k -> registerPlaceholder("${" + k + "}", () -> args.get(k)));
        registerPlaceholder("{has-back-menu}", () -> String.valueOf(previousMenu != null));
        if (config.getAnimation() != null) {
            animator = config.getAnimation().createAnimator();
        }
    }

    public void open() {
        if (!viewer.isOnline()) {
            throw new IllegalStateException("Player is not online");
        }
        if (inventory == null) {
            createInventory(config.getSize(), loader.getMessage().componentBuilder(replace(config.getTitle())), config.getInvType());
        }
        sync(() -> {
            if (!Objects.equals(viewer.getOpenInventory().getTopInventory(), inventory)) {
                viewer.openInventory(inventory);
            }
            Arrays.fill(matrix, null);
            Arrays.fill(animationMask, null);
            generate0();
            /*if (openRequirements != null && !openRequirements.check(Menu.this, viewer)) {
                List<String> list = new ArrayList<>(openRequirements.getDenyCommands());
                list.replaceAll(this::replace);
                runCommands(list);
            } else {
                if (!openCommands.isEmpty()) runCommands(openCommands);
                viewer.openInventory(inventory);
                generate0();
            }*/
        });
        if (animator != null) {
            if (animationTask != null) {
                animationTask.cancel();
            }
            animationTask = Bukkit.getScheduler().runTaskTimer(
                    loader.getPlugin(),
                    this::animationTick,
                    0,
                    1
            );
        }
    }

    private void animationTick() {
        animator.tick(animationMask, this);
        inventory.clear();
        flush();
        if (animator.isEnd()) {
            animationTask.cancel();
            animationTask = null;
        }
    }

    public void reopen() {
        open();
    }

    public void refresh() {
        generate0();
    }

    protected abstract void generate();

    protected void generate0() {
        Arrays.fill(matrix, null);
        config.generate(this);
        generate();
        sendFakeTitle(config.getTitle());
        inventory.clear();
        flush();
    }

    protected void flush() {
        setMatrix(matrix);
        setMatrix(animationMask);
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
            setItem(menuItem);
        }
    }

    public void setItem(MenuItem item) {
        for (int slot : item.getSlots()) {
            if (slot == -1) continue;
            matrix[slot] = item;
        }
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
        return super.replace(loader.getMessage().setPlaceholders(viewer, string));
    }

    protected abstract boolean runCommand(String cmd) throws CommandException;

    public void runCommands(List<String> commands) {
        for (String command : commands) {
            try {
                if (!runCommand(command)) {
                    Menu.commands.process(this, new StringReader(command));
                }
            } catch (CommandException e) {
                loader.getLogger().error("Failed to run command: {}", command, e);
            }
        }
    }

    public void onClose(InventoryCloseEvent event) {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
    }

    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getCurrentItem() == null) {
            return;
        }
        if (!Objects.equals(inventory, e.getClickedInventory())) return;
        if (e.getSlot() >= matrix.length) return;
        MenuItem item = matrix[e.getSlot()];
        if (item == null) return;
        MenuClickType type = MenuClickType.getClickType(e);
        item.doClick(this, viewer, type);
    }

    public void onClick(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    protected void sync(Runnable runnable) {
        if (!Bukkit.isPrimaryThread())
            Bukkit.getScheduler().runTaskLater(loader.getPlugin(), runnable, 0);
        else runnable.run();
    }

    protected void sendFakeTitle(String title) {
        BLib.getApi().getFakeTitleFactory().get().send(inventory, loader.getMessage().componentBuilder(replace(title)));
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

    private static void runIn(String rawNBT, Menu menu, MenuLoader loader) {
        if (rawNBT != null) {
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
                .argument(new ArgumentStrings<>("cmd"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("cmd");
                            v.sync(() -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd));
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[PLAYER]")
                .argument(new ArgumentStrings<>("cmd"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("cmd");
                            v.sync(() -> v.viewer.performCommand(cmd));
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[SOUND]")
                .argument(new ArgumentEnumValue<>("sound", Sound.class))
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
                .executor((v, args) -> v.sync(v.viewer::closeInventory))
        );
        commands.addSubCommand(new Command<Menu>("[BACK_OR_CLOSE]")
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
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> {
                    var m = Objects.requireNonNull(v.previousMenu, "does not have a previous menu!");
                    if (args.containsKey("commands"))
                        runIn((String) args.get("commands"), m, v.loader);
                    m.reopen();
                })
        );
        commands.addSubCommand(new Command<Menu>("[REFRESH]")
                .executor((v, args) -> v.refresh())
        );
        commands.addSubCommand(new Command<Menu>("[MESSAGE]")
                .argument(new ArgumentStrings<>("msg"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("msg");
                            v.loader.getMessage().sendMsg(v.viewer, cmd);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[OPEN]")
                .argument(new ArgumentString<>("menu"))
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> {
                            String menu = (String) args.getOrThrow("menu", "PluginUser [OPEN_MENU] <menu id>");
                            MenuConfig settings;
                            if (menu.contains(":")) {
                                settings = v.loader.findMenu(new SpacedNameKey(menu));
                            } else {
                                settings = v.loader.findMenuByName(menu);
                            }
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
    }
}
