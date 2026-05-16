package com.wemmbu.shots.commands;

import com.wemmbu.shots.ShotItems;
import com.wemmbu.shots.WemmbuShots;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NukeShotCommand implements CommandExecutor {

    private final WemmbuShots plugin;

    public NukeShotCommand(WemmbuShots plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Must be a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Permission check (also enforced by plugin.yml, but double-checked here)
        if (!player.hasPermission("nukeshot.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use Nuke Shot!");
            return true;
        }

        // Give the item
        player.getInventory().addItem(ShotItems.createNukeShot());
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "☢ NUKE SHOT " +
                ChatColor.YELLOW + "added to your inventory! " +
                ChatColor.GRAY + "Cast the fishing rod — where the hook lands, a MASSIVE nuke detonates!");
        return true;
    }
}
