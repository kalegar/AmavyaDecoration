package me.sanjy33.amavyadecoration.hook;

import me.sanjy33.amavyaparticlelib.*;
import org.bukkit.Location;
import org.bukkit.Particle;

public class AmavyaParticleLibWrapper implements AmavyaParticleLibHook {

    public void addSpiralEffect(Location location, Particle particle, long duration, float length, double radius) {
        AmavyaParticleLib.addParticleEvent(new SpiralParticleEvent(location, particle, duration, length, radius));
    }

    public void addDoubleSpiralEffect(Location location, Particle particle, long duration, float length, double radius) {
        AmavyaParticleLib.addParticleEvent(new DoubleSpiralParticleEvent(location, particle, duration, length, radius));
    }

    public void addBurstEffect(Location location, Particle particle, long duration, float period, double radius, double maxRadius, int steps, Object data) {
        ParticleEvent event = new BurstParticleEvent(location, particle, duration, period, radius, maxRadius, steps);
        event.setData(data);
        AmavyaParticleLib.addParticleEvent(event);
    }

}
