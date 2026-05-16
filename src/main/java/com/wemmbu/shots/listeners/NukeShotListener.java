package com.wemmbu.shots.listeners;

import com.wemmbu.shots.ShotItems;
import com.wemmbu.shots.WemmbuShots;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class NukeShotListener implements Listener {

    // Sphere radius of the blast in blocks — truly MASSIVE
    private static final int    BLAST_RADIUS = 25;
    // Max damage at the epicentre
    private static final double BLAST_DAMAGE = 40.0;
    // How many ticks between each expanding ring of block removal (smaller = faster)
    private static final long   RING_DELAY   = 1L;

    private final WemmbuShots plugin;

    public NukeShotListener(WemmbuShots plugin) {
        this.plugin = plugin;
    }

    /**
     * Fired whenever a fishing rod state changes. We care about CAUGHT_FISH (hook hit ground/water)
     * and IN_GROUND (hook stuck in a block) — both mean the hook has landed somewhere.
     */
    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        // Only react when the hook has landed (hit ground or water)
        PlayerFishEvent.State state = event.getState();
        if (state != PlayerFishEvent.State.CAUGHT_FISH
                && state != PlayerFishEvent.State.IN_GROUND
                && state != PlayerFishEvent.State.FAILED_ATTEMPT) return;

        // Check the item in the player's main hand
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!ShotItems.isNukeShot(hand)) {
            // Also check off-hand
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (!ShotItems.isNukeShot(offHand)) return;
        }

        // Permission check
        if (!player.hasPermission("nukeshot.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use Nuke Shot!");
            return;
        }

        FishHook hook = event.getHook();
        Location impact = hook.getLocation().clone();

        // Cancel the fishing action so no fish is caught, rod stays
        event.setCancelled(true);
        hook.remove();

        // Detonate!
        detonate(player, impact);
    }

    private void detonate(Player shooter, Location impact) {

        // ── Announce ────────────────────────────────────────────────────────
        shooter.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "☢ NUKE SHOT DETONATING!");
        impact.getWorld().playSound(impact, Sound.ENTITY_LIGHTNING_THUNDER, 10f, 0.4f);
        impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE,   10f, 0.3f);

        // ── Expanding sphere of destruction ─────────────────────────────────
        // We expand ring by ring outward (radius 1 → BLAST_RADIUS) over time
        // for a dramatic cinematic mushroom-cloud feel.
        new BukkitRunnable() {
            int currentRadius = 1;

            @Override
            public void run() {
                if (currentRadius > BLAST_RADIUS) {
                    // Final massive particle burst when fully expanded
                    impact.getWorld().spawnParticle(Particle.EXPLOSION_HUGE,  impact, 30, BLAST_RADIUS / 2.0, BLAST_RADIUS / 2.0, BLAST_RADIUS / 2.0, 0.05);
                    impact.getWorld().spawnParticle(Particle.SMOKE_LARGE,     impact, 200, BLAST_RADIUS / 1.5, BLAST_RADIUS / 1.5, BLAST_RADIUS / 1.5, 0.08);
                    impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 10f, 0.2f);
                    cancel();
                    return;
                }

                int r = currentRadius;

                // Remove all blocks in this shell (sphere surface at radius r)
                for (int x = -r; x <= r; x++) {
                    for (int y = -r; y <= r; y++) {
                        for (int z = -r; z <= r; z++) {
                            double dist = Math.sqrt(x * x + y * y + z * z);
                            // Only clear blocks whose distance is within this ring (shell)
                            if (dist >= r - 0.5 && dist <= r + 0.5) {
                                Location blockLoc = impact.clone().add(x, y, z);
                                Material mat = blockLoc.getBlock().getType();
                                // Never destroy bedrock
                                if (mat != Material.AIR && mat != Material.BEDROCK) {
                                    blockLoc.getBlock().setType(Material.AIR);
                                }
                            }
                        }
                    }
                }

                // Also clear everything strictly inside the sphere (fill in any missed interior)
                for (int x = -r; x <= r; x++) {
                    for (int y = -r; y <= r; y++) {
                        for (int z = -r; z <= r; z++) {
                            if (x * x + y * y + z * z <= r * r) {
                                Location blockLoc = impact.clone().add(x, y, z);
                                Material mat = blockLoc.getBlock().getType();
                                if (mat != Material.AIR && mat != Material.BEDROCK) {
                                    blockLoc.getBlock().setType(Material.AIR);
                                }
                            }
                        }
                    }
                }

                // Expanding ring particles & sound
                impact.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, impact, 8,
                        r * 0.6, r * 0.6, r * 0.6, 0.1);
                impact.getWorld().spawnParticle(Particle.FLAME, impact, 20,
                        r * 0.7, r * 0.4, r * 0.7, 0.15);
                impact.getWorld().spawnParticle(Particle.SMOKE_LARGE, impact, 15,
                        r * 0.6, r * 0.6, r * 0.6, 0.08);

                if (currentRadius % 5 == 0) {
                    impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 8f,
                            0.5f + (currentRadius / (float) BLAST_RADIUS));
                }

                currentRadius++;
            }
        }.runTaskTimer(plugin, 0L, RING_DELAY);

        // ── Entity damage (dealt immediately at cast time) ───────────────────
        Collection<Entity> nearby = impact.getWorld()
                .getNearbyEntities(impact, BLAST_RADIUS, BLAST_RADIUS, BLAST_RADIUS);
        int entitiesHit = 0;
        for (Entity entity : nearby) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity.equals(shooter)) continue;

            LivingEntity living = (LivingEntity) entity;
            double distance = entity.getLocation().distance(impact);
            double scaledDamage = BLAST_DAMAGE * Math.max(0.1, 1.0 - distance / (BLAST_RADIUS * 1.2));

            living.damage(scaledDamage, shooter);
            living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,      120, 4));
            living.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 160, 1));

            org.bukkit.util.Vector kb = entity.getLocation().toVector()
                    .subtract(impact.toVector()).normalize().multiply(3.5).setY(1.8);
            entity.setVelocity(kb);
            entitiesHit++;
        }

        shooter.sendMessage(ChatColor.RED + "☢ " + ChatColor.YELLOW + "NUKE SHOT at "
                + ChatColor.WHITE + "(" + impact.getBlockX() + ", "
                + impact.getBlockY() + ", " + impact.getBlockZ() + ")"
                + ChatColor.GRAY + " — " + entitiesHit + " entities obliterated! "
                + ChatColor.DARK_GRAY + "(r=" + BLAST_RADIUS + ")");
    }
}
