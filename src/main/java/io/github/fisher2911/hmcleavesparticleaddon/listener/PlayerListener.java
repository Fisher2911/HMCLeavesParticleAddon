package io.github.fisher2911.hmcleavesparticleaddon.listener;

import com.hibiscusmc.hmcleaves.world.ChunkPosition;
import io.github.fisher2911.hmcleavesparticleaddon.particle.ParticleChunkTracker;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {

    private final ParticleChunkTracker tracker;

    public PlayerListener(ParticleChunkTracker tracker) {
        this.tracker = tracker;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Chunk chunk = event.getPlayer().getLocation().getChunk();
        final ChunkPosition position = this.getChunkPosition(chunk);
        this.tracker.addPlayer(event.getPlayer(), position);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        final Location to = event.getTo();
        final Location from = event.getFrom();
        if (to == null || from.getChunk().equals(to.getChunk())) return;
        final ChunkPosition toPosition = this.getChunkPosition(to.getChunk());
        final ChunkPosition fromPosition = this.getChunkPosition(from.getChunk());
        this.tracker.movePlayer(event.getPlayer(), fromPosition, toPosition);
    }

    private ChunkPosition getChunkPosition(Chunk chunk) {
        return new ChunkPosition(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
    }

}
