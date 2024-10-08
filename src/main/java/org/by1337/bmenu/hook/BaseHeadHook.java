package org.by1337.bmenu.hook;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class BaseHeadHook {

    private static final Logger LOGGER = LoggerFactory.getLogger("BMenu#BaseHeadHook");

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
        }
        try {
            return new ItemStack(Material.valueOf(argument));
        } catch (IllegalArgumentException e) {
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
