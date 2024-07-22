package io.github.fisher2911.hmcleavesparticleaddon.particle;

import com.hibiscusmc.hmcleaves.api.HMCLeavesAPI;
import com.hibiscusmc.hmcleaves.block.BlockData;
import com.hibiscusmc.hmcleaves.block.BlockType;
import com.hibiscusmc.hmcleaves.packetevents.PacketEvents;
import com.hibiscusmc.hmcleaves.packetevents.protocol.particle.Particle;
import com.hibiscusmc.hmcleaves.packetevents.protocol.particle.data.ParticleBlockStateData;
import com.hibiscusmc.hmcleaves.packetevents.protocol.particle.type.ParticleTypes;
import com.hibiscusmc.hmcleaves.packetevents.protocol.world.states.WrappedBlockState;
import com.hibiscusmc.hmcleaves.packetevents.util.Vector3d;
import com.hibiscusmc.hmcleaves.packetevents.util.Vector3f;
import com.hibiscusmc.hmcleaves.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import com.hibiscusmc.hmcleaves.world.ChunkPosition;
import com.hibiscusmc.hmcleaves.world.LeavesChunk;
import com.hibiscusmc.hmcleaves.world.Position;
import com.hibiscusmc.hmcleaves.world.PositionInChunk;
import io.github.fisher2911.hmcleavesparticleaddon.HMCLeavesParticleAddon;
import io.github.fisher2911.hmcleavesparticleaddon.config.ParticleConfig;
import io.github.fisher2911.hmcleavesparticleaddon.metadata.LeavesMetadata;
import io.github.fisher2911.hmcleavesparticleaddon.util.ChunkMetadataUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ParticleChunkTracker {

    private static final List<RandomPosition> randomPositions = new ArrayList<>();

    static {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                randomPositions.add(new RandomPosition(x, 0, z));
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
        final LeavesChunk fromChunk = this.api.getLeavesChunk(from);
        if (fromChunk == null) {
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
                final LeavesChunk leavesChunk = this.api.getLeavesChunk(chunkPosition);
                if (leavesChunk == null) {
                    return true;
                }
                final Collection<PositionInChunk> particlePositions = leavesChunk.getMetadata().get(LeavesMetadata.PARTICLE_POSITIONS);
                final Map<UUID, Integer> players = leavesChunk.getMetadata().get(LeavesMetadata.PLAYERS);
                if (players == null) {
                    return false;
                }
                if (particlePositions == null) {
                    this.addRandomParticlePositions(leavesChunk);
                    return false;
                }
                List<PositionInChunk> positions = new ArrayList<>(particlePositions);
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
                        final BlockData blockData = leavesChunk.get(position);
                        final WrappedBlockState blockState = this.config.getLeafParticle(blockData.getId());
                        if (blockState == null) {
                            particlePositions.remove(position);
                            return;
                        }
                        PacketEvents.getAPI().getPlayerManager().sendPacket(
                                player,
                                new WrapperPlayServerParticle(
                                        new Particle(ParticleTypes.FALLING_DUST, new ParticleBlockStateData(blockState)),
                                        true,
                                        new Vector3d(position.getX() + 0.5, position.getY(), position.getZ() + 0.5),
                                        Vector3f.zero(),
                                        1,
                                        1
                                )
                        );
                    });
                    if (originalSize != particlePositions.size()) {
                        leavesChunk.getMetadata().remove(LeavesMetadata.PARTICLE_POSITIONS);
                        this.addRandomParticlePositions(leavesChunk);
                    }
                }
                playersToRemove.forEach(players::remove);
                return false;
            });
        }, this.config.getParticleSendTickRate(), this.config.getParticleSendTickRate());
    }

    private void addRandomParticlePositions(LeavesChunk leavesChunk) {
        final World world = Bukkit.getWorld(leavesChunk.getWorld());
        if (world == null) return;
        final int minHeight = world.getMinHeight();
        final int maxHeight = world.getMaxHeight();
        int totalParticles = this.config.getParticlesPerChunk();
        final List<RandomPosition> random = new ArrayList<>(randomPositions);
        Collections.shuffle(random);
        final List<PositionInChunk> positions = new ArrayList<>();
        for (final var entry : leavesChunk.getBlocks().entrySet()) {
            if (totalParticles <= 0) break;
            final PositionInChunk position = entry.getKey();
            final BlockData blockData = entry.getValue();
            if (this.config.getLeafParticle(blockData.getId()) == null) continue;
            if (!random.contains(new RandomPosition(position.getX(), 0, position.getZ()))) {
                continue;
            }
            PositionInChunk particlePosition = position;
            PositionInChunk relativePosition = position.relative(BlockFace.DOWN, minHeight, maxHeight);
            while (relativePosition != null) {
                final BlockData relativeData = leavesChunk.get(relativePosition);
                if (relativeData == null || relativeData.getBlockType() != BlockType.LEAVES) break;
                particlePosition = relativePosition;
                relativePosition = relativePosition.relative(BlockFace.DOWN, minHeight, maxHeight);
            }
            positions.add(particlePosition);
            totalParticles--;
        }
        Collections.shuffle(positions);
        leavesChunk.getMetadata().set(LeavesMetadata.PARTICLE_POSITIONS, positions);
    }

    private record RandomPosition(int x, int y, int z) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RandomPosition that = (RandomPosition) o;
            return x == that.x && y == that.y && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

}
