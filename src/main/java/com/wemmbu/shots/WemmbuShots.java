package com.wemmbu.shots;

import com.wemmbu.shots.commands.NukeShotCommand;
import com.wemmbu.shots.commands.StabShotCommand;
import com.wemmbu.shots.listeners.NukeShotListener;
import com.wemmbu.shots.listeners.StabShotListener;
import org.bukkit.plugin.java.JavaPlugin;

public class WemmbuShots extends JavaPlugin {

    private static WemmbuShots instance;

    @Override
    public void onEnable() {
        instance = this;

        // Register commands
        getCommand("nukeshot").setExecutor(new NukeShotCommand(this));
        getCommand("stabshot").setExecutor(new StabShotCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new NukeShotListener(this), this);
        getServer().getPluginManager().registerEvents(new StabShotListener(this), this);

        getLogger().info("WemmbuShots has been enabled! Nuke Shot & Stab Shot ready.");
    }

    @Override
    public void onDisable() {
        getLogger().info("WemmbuShots has been disabled.");
    }

    public static WemmbuShots getInstance() {
        return instance;
    }
}
