package org.by1337.bmenu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
import org.by1337.bmenu.animation.Animator;
import org.by1337.bmenu.click.MenuClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Menu extends Placeholder implements InventoryHolder {
    private static final Command<Menu> COMMANDS;
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
                    COMMANDS.process(this, new StringReader(command));
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


    static {
        COMMANDS = new Command<>("root");
    }
}
