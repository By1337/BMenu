package org.by1337.bmenu.network;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BungeeCordMessageSender {
    private static final Logger LOGGER = LoggerFactory.getLogger("[BMenu#BungeeCord]");

    private static final String channelName = "BungeeCord";

    public static void connectPlayerToServer(Player player, String toServer, Plugin plugin) {
        try (ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(byteBuff)) {
            out.writeUTF("Connect");
            out.writeUTF(toServer);
            out.flush();
            player.sendPluginMessage(plugin, channelName, byteBuff.toByteArray());
        } catch (IOException e) {
            LOGGER.error("Failed to send packet", e);
        }
    }

}