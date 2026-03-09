package dev.by1337.bmenu.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PlayerInputListener implements Listener {
    private final Plugin plugin;
    private final Map<UUID, Consumer<@Nullable String>> playerInputs = new ConcurrentHashMap<>();

    public PlayerInputListener(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void register(UUID player, Consumer<@Nullable String> c) {
        var task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            var old = playerInputs.remove(player);
            if (old != null) old.accept(null);
        }, 30 * 20);
        var old = playerInputs.put(player, s -> {
            if (!task.isCancelled()) task.cancel();
            c.accept(s);
        });
        if (old != null) {
            old.accept(null);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void on(AsyncPlayerChatEvent event) {
        var c = playerInputs.remove(event.getPlayer().getUniqueId());
        if (c != null) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTask(plugin, () -> c.accept(event.getMessage()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void on(PlayerCommandPreprocessEvent event) {
        var c = playerInputs.remove(event.getPlayer().getUniqueId());
        if (c != null) {
            event.setCancelled(true);
            c.accept(event.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void on(PlayerQuitEvent event) {
        var c = playerInputs.remove(event.getPlayer().getUniqueId());
        if (c != null) {
            c.accept(null);
        }
    }

    public void close() {
        HandlerList.unregisterAll(this);
        new ArrayList<>(playerInputs.values()).forEach(v -> v.accept(null));
        playerInputs.clear();
    }
}
