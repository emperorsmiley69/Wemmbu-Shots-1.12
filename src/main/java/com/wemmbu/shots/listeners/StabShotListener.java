package com.wemmbu.shots.listeners;

import com.wemmbu.shots.ShotItems;
import com.wemmbu.shots.WemmbuShots;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class StabShotListener implements Listener {

    // Radius of the bored cylinder in blocks (1 = 3x3, 2 = 5x5, etc.)
    private static final int  BORE_RADIUS  = 2;
    // Ticks between each layer being removed (1 tick = fast drilling effect)
    private static final long LAYER_DELAY  = 1L;

    private final WemmbuShots plugin;

    public StabShotListener(WemmbuShots plugin) {
        this.plugin = plugin;
    }

    /**
     * Fired on any fishing rod state change.
     * We react when the hook has landed (CAUGHT_FISH, IN_GROUND, FAILED_ATTEMPT).
     */
    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        PlayerFishEvent.State state = event.getState();
        if (state != PlayerFishEvent.State.CAUGHT_FISH
                && state != PlayerFishEvent.State.IN_GROUND
                && state != PlayerFishEvent.State.FAILED_ATTEMPT) return;

        // Check main hand then off-hand for a Stab Shot rod
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!ShotItems.isStabShot(hand)) {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (!ShotItems.isStabShot(offHand)) return;
        }

        // Permission check
        if (!player.hasPermission("stabshot.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use Stab Shot!");
            return;
        }

        Location hookLoc = event.getHook().getLocation().clone();

        // Cancel fishing so no fish pops out
        event.setCancelled(true);
        event.getHook().remove();

        // Start the bore
        bore(player, hookLoc);
    }

    private void bore(Player shooter, Location top) {

        shooter.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "⚫ STAB SHOT — boring to bedrock!");
        top.getWorld().playSound(top, Sound.ENTITY_WITHER_SHOOT,  3f, 0.5f);
        top.getWorld().playSound(top, Sound.BLOCK_ANVIL_LAND,     3f, 0.4f);

        // Snap X/Z to block centre
        int originX = top.getBlockX();
        int originZ = top.getBlockZ();
        int startY  = top.getBlockY();

        new BukkitRunnable() {
            int currentY = startY;

            @Override
            public void run() {
                // Stop at bedrock (y=0 in 1.12.2, bedrock is at y=0 to y=4)
                if (currentY < 0) {
                    // Finished — play a deep rumble
                    top.getWorld().playSound(top, Sound.ENTITY_GENERIC_EXPLODE, 5f, 0.3f);
                    shooter.sendMessage(ChatColor.AQUA + "⚫ " + ChatColor.YELLOW
                            + "Bore complete! Shaft at "
                            + ChatColor.WHITE + "(" + originX + ", " + startY + " → 0, " + originZ + ")"
                            + ChatColor.GRAY + " r=" + BORE_RADIUS);
                    cancel();
                    return;
                }

                boolean hitBedrock = false;

                // Remove all blocks in the circle at this Y layer
                for (int dx = -BORE_RADIUS; dx <= BORE_RADIUS; dx++) {
                    for (int dz = -BORE_RADIUS; dz <= BORE_RADIUS; dz++) {
                        if (dx * dx + dz * dz <= BORE_RADIUS * BORE_RADIUS) {
                            Location blockLoc = new Location(
                                    top.getWorld(),
                                    originX + dx,
                                    currentY,
                                    originZ + dz
                            );
                            Material mat = blockLoc.getBlock().getType();
                            if (mat == Material.BEDROCK) {
                                hitBedrock = true;
                            } else if (mat != Material.AIR) {
                                blockLoc.getBlock().setType(Material.AIR);
                            }
                        }
                    }
                }

                // Particles at the current drill head
                Location centre = new Location(top.getWorld(), originX + 0.5, currentY + 0.5, originZ + 0.5);
                top.getWorld().spawnParticle(Particle.SMOKE_LARGE,    centre, 12, BORE_RADIUS * 0.4, 0.1, BORE_RADIUS * 0.4, 0.05);
                top.getWorld().spawnParticle(Particle.CRIT_MAGIC,     centre, 8,  BORE_RADIUS * 0.5, 0.1, BORE_RADIUS * 0.5, 0.1);
                top.getWorld().spawnParticle(Particle.SPELL_INSTANT,  centre, 6,  BORE_RADIUS * 0.3, 0.1, BORE_RADIUS * 0.3, 0.05);

                // Drilling sound every few layers
                if (currentY % 4 == 0) {
                    top.getWorld().playSound(centre, Sound.BLOCK_STONE_BREAK, 1.5f, 0.8f);
                }

                // If we've reached bedrock, stop next tick
                if (hitBedrock) {
                    currentY = -1; // triggers cancel on next iteration
                } else {
                    currentY--;
                }
            }
        }.runTaskTimer(plugin, 0L, LAYER_DELAY);
    }
}
