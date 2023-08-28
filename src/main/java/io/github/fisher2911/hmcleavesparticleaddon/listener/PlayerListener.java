package io.github.fisher2911.hmcleavesparticleaddon.listener;

import io.github.fisher2911.hmcleaves.world.ChunkPosition;
import io.github.fisher2911.hmcleavesparticleaddon.particle.ParticleChunkTracker;
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
        final ChunkPosition position = ChunkPosition.fromChunk(event.getPlayer().getLocation().getChunk());
        this.tracker.addPlayer(event.getPlayer(), position);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        final Location to = event.getTo();
        final Location from = event.getFrom();
        if (to == null || from.getChunk().equals(to.getChunk())) return;
        final ChunkPosition toPosition = ChunkPosition.fromChunk(to.getChunk());
        final ChunkPosition fromPosition = ChunkPosition.fromChunk(from.getChunk());
        this.tracker.movePlayer(event.getPlayer(), fromPosition, toPosition);
    }


}
