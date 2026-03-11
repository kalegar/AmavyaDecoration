package me.sanjy33.amavyadecoration.hook;

import org.bukkit.Location;
import org.bukkit.Particle;

public interface AmavyaParticleLibHook {

    void addSpiralEffect(Location location, Particle particle, long duration, float length, double radius);

    void addBurstEffect(Location location, Particle particle, long duration, float period, double radius, double maxRadius, int steps, Object data);

    void addDoubleSpiralEffect(Location location, Particle particle, long duration, float length, double radius);
}