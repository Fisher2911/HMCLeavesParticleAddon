package io.github.fisher2911.hmcleavesparticleaddon.particle;

import io.github.fisher2911.hmcleaves.api.HMCLeavesAPI;
import io.github.fisher2911.hmcleaves.cache.ChunkBlockCache;
import io.github.fisher2911.hmcleaves.data.BlockData;
import io.github.fisher2911.hmcleaves.data.LeafData;
import io.github.fisher2911.hmcleaves.packetevents.api.PacketEvents;
import io.github.fisher2911.hmcleaves.packetevents.api.protocol.particle.Particle;
import io.github.fisher2911.hmcleaves.packetevents.api.protocol.particle.data.ParticleBlockStateData;
import io.github.fisher2911.hmcleaves.packetevents.api.protocol.particle.type.ParticleTypes;
import io.github.fisher2911.hmcleaves.packetevents.api.protocol.world.states.WrappedBlockState;
import io.github.fisher2911.hmcleaves.packetevents.api.util.Vector3d;
import io.github.fisher2911.hmcleaves.packetevents.api.util.Vector3f;
import io.github.fisher2911.hmcleaves.packetevents.api.wrapper.play.server.WrapperPlayServerParticle;
import io.github.fisher2911.hmcleaves.util.ChunkUtil;
import io.github.fisher2911.hmcleaves.world.ChunkPosition;
import io.github.fisher2911.hmcleaves.world.Position;
import io.github.fisher2911.hmcleavesparticleaddon.HMCLeavesParticleAddon;
import io.github.fisher2911.hmcleavesparticleaddon.config.ParticleConfig;
import io.github.fisher2911.hmcleavesparticleaddon.metadata.LeavesMetadata;
import io.github.fisher2911.hmcleavesparticleaddon.util.ChunkMetadataUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ParticleChunkTracker {

    private static final List<Position> randomPositions = new ArrayList<>();

    static {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                randomPositions.add(Position.at(null, x, 0, z));
            }
        }
    }

    private final HMCLeavesParticleAddon plugin;
    private final ParticleConfig config;
    private final HMCLeavesAPI api;
    private final Set<ChunkPosition> chunksWithParticles;
    private BukkitTask task;

    public ParticleChunkTracker(HMCLeavesParticleAddon plugin, Set<ChunkPosition> chunksWithParticles) {
        this.plugin = plugin;
        this.config = plugin.getParticleConfig();
        this.api = HMCLeavesAPI.getInstance();
        this.chunksWithParticles = chunksWithParticles;
    }

    public void movePlayer(Player player, ChunkPosition from, ChunkPosition to) {
        final ChunkBlockCache fromCache = this.api.getChunkBlockCache(from);
        if (fromCache == null) {
            this.chunksWithParticles.remove(from);
        }
        final Collection<ChunkPosition> removed = ChunkMetadataUtil.removePlayerFromChunk(this.api, from, player);
        final Collection<ChunkPosition> added = ChunkMetadataUtil.addPlayerToChunk(this.api, to, player);
        removed.removeAll(added);
        this.chunksWithParticles.removeAll(removed);
        this.chunksWithParticles.addAll(added);
    }

    public void addPlayer(Player player, ChunkPosition position) {
        this.chunksWithParticles.addAll(
                ChunkMetadataUtil.addPlayerToChunk(this.api, position, player)
        );
    }

    public void stopTimer() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

    public void startTimer() {
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, () -> {
            this.chunksWithParticles.removeIf(chunkPosition -> {
                final ChunkBlockCache cache = this.api.getChunkBlockCache(chunkPosition);
                if (cache == null) {
                    return true;
                }
                final Collection<Position> particlePositions = cache.getMetadata().get(LeavesMetadata.PARTICLE_POSITIONS);
                final Map<UUID, Integer> players = cache.getMetadata().get(LeavesMetadata.PLAYERS);
                if (players == null) {
                    return false;
                }
                if (particlePositions == null) {
                    this.addRandomParticlePositions(cache);
                    return false;
                }
                List<Position> positions = new ArrayList<>(particlePositions);
                Collections.shuffle(positions);
                positions = positions.subList(0, Math.min(this.config.getSendParticlesPerChunk(), positions.size()));
                final int originalSize = particlePositions.size();
                final Set<UUID> playersToRemove = new HashSet<>();
                for (final UUID uuid : players.keySet()) {
                    final Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        playersToRemove.add(uuid);
                        continue;
                    }
                    positions.forEach(position -> {
                        final BlockData blockData = cache.getBlockDataAt(position);
                        final WrappedBlockState blockState = this.config.getLeafParticle(blockData.id());
                        if (blockState == null) {
                            particlePositions.remove(position);
                            return;
                        }
                        PacketEvents.getAPI().getPlayerManager().sendPacket(
                                player,
                                new WrapperPlayServerParticle(
                                        new Particle(ParticleTypes.FALLING_DUST, new ParticleBlockStateData(blockState)),
                                        true,
                                        new Vector3d(position.x() + 0.5, position.y(), position.z() + 0.5),
                                        Vector3f.zero(),
                                        1,
                                        1
                                )
                        );
                    });
                    if (originalSize != particlePositions.size()) {
                        cache.getMetadata().remove(LeavesMetadata.PARTICLE_POSITIONS);
                        this.addRandomParticlePositions(cache);
                    }
                }
                playersToRemove.forEach(players::remove);
                return false;
            });
        }, this.config.getParticleSendTickRate(), this.config.getParticleSendTickRate());
    }

    private void addRandomParticlePositions(ChunkBlockCache cache) {
        int totalParticles = this.config.getParticlesPerChunk();
        final List<Position> random = new ArrayList<>(randomPositions);
        Collections.shuffle(random);
        final List<Position> positions = new ArrayList<>();
        for (final var entry : cache.getBlockDataMap().entrySet()) {
            if (totalParticles <= 0) break;
            final Position position = entry.getKey();
            final BlockData blockData = entry.getValue();
            if (this.config.getLeafParticle(blockData.id()) == null) continue;
            if (!random.contains(Position.at(null, ChunkUtil.getCoordInChunk(position.x()), 0, ChunkUtil.getCoordInChunk(position.z())))) {
                continue;
            }
            Position particlePosition = position;
            while (cache.getBlockDataAt(particlePosition.getRelative(BlockFace.DOWN)) instanceof LeafData) {
                particlePosition = particlePosition.getRelative(BlockFace.DOWN);
            }
            positions.add(particlePosition);
            totalParticles--;
        }
        Collections.shuffle(positions);
        cache.getMetadata().set(LeavesMetadata.PARTICLE_POSITIONS, positions);
    }

}
