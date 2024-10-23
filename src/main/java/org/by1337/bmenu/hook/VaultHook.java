package org.by1337.bmenu.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Objects;
import java.util.UUID;

public class VaultHook {
    private static final VaultHook INSTANCE = new VaultHook();
    private final Economy econ;

    public VaultHook() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            econ = null;
        } else {
            econ = rsp.getProvider();
        }
    }
    public boolean isAvailable(){
        return econ != null;
    }

    public double getBalance(UUID uuid) {
        return getBalance(Bukkit.getOfflinePlayer(uuid));
    }

    public void withdrawPlayer(UUID uuid, double count) {
        withdrawPlayer(Bukkit.getOfflinePlayer(uuid), count);
    }

    public void depositPlayer(UUID uuid, double count) {
        depositPlayer(Bukkit.getOfflinePlayer(uuid), count);
    }

    public double getBalance(OfflinePlayer offlinePlayer) {
        Objects.requireNonNull(econ, "Economy not defined");
        return econ.getBalance(offlinePlayer);
    }

    public void withdrawPlayer(OfflinePlayer offlinePlayer, double count) {
        Objects.requireNonNull(econ, "Economy not defined");
        econ.withdrawPlayer(offlinePlayer, count);
    }

    public void depositPlayer(OfflinePlayer offlinePlayer, double count) {
        Objects.requireNonNull(econ, "Economy not defined");
        econ.depositPlayer(offlinePlayer, count);
    }

    public static VaultHook get() {
        return INSTANCE;
    }
}
