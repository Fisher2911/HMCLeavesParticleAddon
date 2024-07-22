package io.github.fisher2911.hmcleavesparticleaddon.util;

import com.hibiscusmc.hmcleaves.api.HMCLeavesAPI;
import com.hibiscusmc.hmcleaves.world.ChunkPosition;
import com.hibiscusmc.hmcleaves.world.LeavesChunk;
import io.github.fisher2911.hmcleavesparticleaddon.metadata.LeavesMetadata;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class ChunkMetadataUtil {

    /**
     *
     * @return collection of chunks with players that was caused by this addition
     */
    public static Collection<ChunkPosition> addPlayerToChunk(HMCLeavesAPI api, ChunkPosition center, Player player) {
        final Collection<ChunkPosition> added = new HashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                final ChunkPosition chunkPosition = new ChunkPosition(center.getWorld(), x + center.getX(), z + center.getZ());
                final LeavesChunk leavesChunk = api.getLeavesChunk(chunkPosition);
                if (leavesChunk == null) {
                    continue;
                }
                Map<UUID, Integer> players = leavesChunk.getMetadata().get(LeavesMetadata.PLAYERS);
                if (players == null) {
                    players = new HashMap<>();
                    leavesChunk.getMetadata().set(LeavesMetadata.PLAYERS, players);
                }
                players.merge(player.getUniqueId(), 1, Integer::sum);
                if (players.size() > 0) {
                    added.add(chunkPosition);
                }
            }
        }
        return added;
    }

    /**
     *
     * @return collection of chunks with zero players that was caused by this removal
     */
    public static Collection<ChunkPosition> removePlayerFromChunk(HMCLeavesAPI api, ChunkPosition center, Player player) {
        final Collection<ChunkPosition> removed = new HashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                final ChunkPosition chunkPosition = new ChunkPosition(center.getWorld(), x + center.getX(), z + center.getZ());
                final LeavesChunk leavesChunk = api.getLeavesChunk(chunkPosition);
                if (leavesChunk == null) continue;
                Map<UUID, Integer> players = leavesChunk.getMetadata().get(LeavesMetadata.PLAYERS);
                if (players == null) continue;
                players.merge(player.getUniqueId(), -1, Integer::sum);
                if (players.get(player.getUniqueId()) <= 0) {
                    players.remove(player.getUniqueId());
                }
                if (players.isEmpty()) {
                    removed.add(chunkPosition);
                }
            }
        }
        return removed;
    }

    public static int countPlayersInChunk(LeavesChunk leavesChunk) {
        Map<UUID, Integer> players = leavesChunk.getMetadata().get(LeavesMetadata.PLAYERS);
        if (players == null) return 0;
        return players.size();
    }

}
