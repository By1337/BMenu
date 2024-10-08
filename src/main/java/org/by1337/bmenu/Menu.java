package org.by1337.bmenu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.StringReader;
import org.by1337.bmenu.click.MenuClickType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Menu implements Placeholderable {
    private static final Command<Menu> COMMANDS;
    protected final MenuConfig config;
    protected final MenuLoader loader;
    protected final Player viewer;
    protected Inventory inventory;
    protected final MenuItem[] matrix;
    protected final Map<String, String> args;
    protected Placeholder argsReplacer;
    @Nullable
    protected final Menu previousMenu;

    public Menu(MenuConfig config, Player viewer, @Nullable Menu previousMenu) {
        this.config = config;
        loader = config.getLoader();
        this.viewer = viewer;
        matrix = new MenuItem[config.getSize()];
        args = new HashMap<>(config.getArgs());
        this.previousMenu = previousMenu;
        argsReplacer = new Placeholder();
        args.keySet().forEach(k -> argsReplacer.replace(args.get(k)));
    }

    public void open() {
        if (inventory == null) {
            createInventory(config.getSize(), loader.getMessage().componentBuilder(replace(config.getTitle())), config.getInvType());
        }
        sync(() -> {
            viewer.openInventory(inventory);
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
        flush();
    }

    protected void flush() {
        for (int i = 0; i < matrix.length; i++) {
            MenuItem item = matrix[i];
            if (item == null) {
                inventory.setItem(i, null);
            } else {
                if (!Objects.equals(inventory.getItem(i), item.getItemStack())) {
                    inventory.setItem(i, item.getItemStack());
                }
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
            matrix[slot] = item;
        }
    }

    protected void createInventory(int size, Component title, InventoryType type) {
        if (type == InventoryType.CHEST) {
            inventory = Bukkit.createInventory(null, size, title);
        } else {
            inventory = Bukkit.createInventory(null, type, title);
        }
    }

    @Override
    public String replace(String string) {
        return argsReplacer.replace(loader.getMessage().setPlaceholders(viewer, string));
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
        MenuClickType type = MenuClickType.getClickType(e.getClick());
        if (type != null)
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

    public Inventory getInventory() {
        return inventory;
    }

    public MenuItem[] getMatrix() {
        return matrix;
    }

    static {
        COMMANDS = new Command<>("root");
    }
}
