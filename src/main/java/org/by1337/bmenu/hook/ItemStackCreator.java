package org.by1337.bmenu.hook;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ItemStackCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger("BMenu#ItemCreator");

    public static ItemStack getItem(String argument) {
        if (argument == null) {
            return new ItemStack(Material.PLAYER_HEAD);
        }
        if (argument.startsWith("basehead-")) {
            try {
                return SkullUtils.getSkull(argument.replace("basehead-", ""));
            } catch (Exception exception) {
                LOGGER.error("Failed to load skull", exception);
                return new ItemStack(Material.PLAYER_HEAD);
            }
        } else if (argument.startsWith("player-")) {
            String value = argument.substring("player-".length());
            if (value.length() == 36) { // is UUID?
                try {
                    UUID uuid = UUID.fromString(value);
                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    head.editMeta(m -> ((SkullMeta) (m)).setOwningPlayer(Bukkit.getOfflinePlayer(uuid)));
                    return head;
                } catch (Throwable ignore) {
                }
            }
            Player player = Bukkit.getPlayer(value);
            if (player == null) {
                LOGGER.error("Failed to get player {}", value);
                return new ItemStack(Material.PLAYER_HEAD);
            }
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            head.editMeta(m -> ((SkullMeta) (m)).setOwningPlayer(player));
            return head;
        }
        try {
            return new ItemStack(Material.valueOf(argument));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Failed to load item {}", argument);
            return new ItemStack(Material.DIRT);
        }
    }

    public static class SkullUtils {
        @NotNull
        public static ItemStack getSkull(@NotNull String skinUrl) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            head.editMeta(m -> {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.setProperty(new ProfileProperty("textures", skinUrl));
                ((SkullMeta) (m)).setPlayerProfile(
                        profile
                );
            });
            return head;
        }
    }
}
