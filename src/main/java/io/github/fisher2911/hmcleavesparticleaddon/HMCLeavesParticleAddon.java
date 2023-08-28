package io.github.fisher2911.hmcleavesparticleaddon;

import io.github.fisher2911.hmcleavesparticleaddon.config.ParticleConfig;
import io.github.fisher2911.hmcleavesparticleaddon.listener.PlayerListener;
import io.github.fisher2911.hmcleavesparticleaddon.particle.ParticleChunkTracker;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public class HMCLeavesParticleAddon extends JavaPlugin {

    private ParticleChunkTracker tracker;
    private ParticleConfig config;

    @Override
    public void onEnable() {
        this.config = new ParticleConfig(this);
        this.config.load();
        this.tracker = new ParticleChunkTracker(this, new HashSet<>());
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this.tracker), this);
        this.tracker.startTimer();
    }

    @Override
    public void onDisable() {
        this.tracker.stopTimer();
    }

    public ParticleChunkTracker getTracker() {
        return this.tracker;
    }

    public ParticleConfig getParticleConfig() {
        return this.config;
    }

}
