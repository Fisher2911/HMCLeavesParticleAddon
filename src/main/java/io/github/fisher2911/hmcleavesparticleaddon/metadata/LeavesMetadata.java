package io.github.fisher2911.hmcleavesparticleaddon.metadata;

import com.hibiscusmc.hmcleaves.HMCLeaves;
import com.hibiscusmc.hmcleaves.util.MetadataKey;
import com.hibiscusmc.hmcleaves.world.Position;
import com.hibiscusmc.hmcleaves.world.PositionInChunk;
import io.github.fisher2911.hmcleavesparticleaddon.HMCLeavesParticleAddon;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class LeavesMetadata {

    private static final JavaPlugin PLUGIN = JavaPlugin.getPlugin(HMCLeavesParticleAddon.class);

    public static final MetadataKey<Map<UUID, Integer>> PLAYERS = (MetadataKey<Map<UUID, Integer>>) createKey("players", Map.class);
    public static final MetadataKey<Collection<PositionInChunk>> PARTICLE_POSITIONS = (MetadataKey<Collection<PositionInChunk>>) createKey("particle_positions", Collection.class);

    private static MetadataKey<?> createKey(String name, Class<?> type) {
        return MetadataKey.of(key(name), type);
    }

    protected static NamespacedKey key(String key) {
        return new NamespacedKey(PLUGIN, key);
    }

}
