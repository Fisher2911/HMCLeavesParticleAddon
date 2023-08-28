package io.github.fisher2911.hmcleavesparticleaddon.metadata;

import io.github.fisher2911.common.metadata.BuiltInMetadata;
import io.github.fisher2911.common.metadata.MetadataKey;
import io.github.fisher2911.hmcleaves.world.Position;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class LeavesMetadata extends BuiltInMetadata {

    public static final MetadataKey<Map<UUID, Integer>> PLAYERS = (MetadataKey<Map<UUID, Integer>>) createKey("players", Map.class);
    public static final MetadataKey<Collection<Position>> PARTICLE_POSITIONS = (MetadataKey<Collection<Position>>) createKey("particle_positions", Collection.class);

    private static MetadataKey<?> createKey(String name, Class<?> type) {
        return MetadataKey.of(key(name), type);
    }

}
