package com.wemmbu.shots.commands;

import com.wemmbu.shots.ShotItems;
import com.wemmbu.shots.WemmbuShots;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StabShotCommand implements CommandExecutor {

    private final WemmbuShots plugin;

    public StabShotCommand(WemmbuShots plugin) {
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

        // Permission check
        if (!player.hasPermission("stabshot.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use Stab Shot!");
            return true;
        }

        // Give the item
        player.getInventory().addItem(ShotItems.createStabShot());
        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "⚫ STAB SHOT " +
                ChatColor.YELLOW + "added to your inventory! " +
                ChatColor.GRAY + "Cast the fishing rod — where the hook lands, a shaft is bored straight to bedrock!");
        return true;
    }
}
