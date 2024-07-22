package io.github.fisher2911.hmcleavesparticleaddon.config;

import com.hibiscusmc.hmcleaves.packetevents.protocol.world.states.WrappedBlockState;
import io.github.fisher2911.hmcleavesparticleaddon.HMCLeavesParticleAddon;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class ParticleConfig {

    private static final String PARTICLES_PER_CHUNK_PATH = "particles-per-chunk";
    private static final String SEND_PARTICLES_PER_CHUNK_PATH = "send-particles-per-chunk";
    private static final String PARTICLE_SEND_TICK_RATE_PATH = "particle-send-tick-rate";
    private static final String LEAF_PARTICLES_PATH = "leaf-particles";

    private final HMCLeavesParticleAddon plugin;

    public ParticleConfig(HMCLeavesParticleAddon plugin) {
        this.plugin = plugin;
    }

    private int particlesPerChunk;
    private int sendParticlesPerChunk;
    private int particleSendTickRate;
    private Map<String, WrappedBlockState> leafParticles;

    public void load() {
        this.plugin.saveDefaultConfig();
        this.particlesPerChunk = this.plugin.getConfig().getInt(PARTICLES_PER_CHUNK_PATH);
        this.sendParticlesPerChunk = this.plugin.getConfig().getInt(SEND_PARTICLES_PER_CHUNK_PATH);
        this.particleSendTickRate = this.plugin.getConfig().getInt(PARTICLE_SEND_TICK_RATE_PATH);
        this.leafParticles = new HashMap<>();
        final ConfigurationSection particleSection = this.plugin.getConfig().getConfigurationSection(LEAF_PARTICLES_PATH);
        if (particleSection == null) return;
        for (String key : particleSection.getKeys(false)) {
            final String value = particleSection.getString(key);
            if (value == null) continue;
            final Material material = Material.matchMaterial(value);
            if (material == null) {
                this.plugin.getLogger().warning("Invalid material: " + value);
                continue;
            }
            final WrappedBlockState state = SpigotConversionUtil.fromBukkitBlockData(material.createBlockData());
            if (state == null) continue;
            this.leafParticles.put(key, state);
        }
    }

    public int getParticlesPerChunk() {
        return this.particlesPerChunk;
    }

    public int getSendParticlesPerChunk() {
        return this.sendParticlesPerChunk;
    }

    public int getParticleSendTickRate() {
        return this.particleSendTickRate;
    }

    public WrappedBlockState getLeafParticle(String id) {
        return this.leafParticles.get(id);
    }

}
