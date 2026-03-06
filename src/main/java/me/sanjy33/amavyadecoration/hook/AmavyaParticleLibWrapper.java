package me.sanjy33.amavyadecoration.hook;

import me.sanjy33.amavyaparticlelib.BurstParticleEvent;
import me.sanjy33.amavyaparticlelib.DoubleSpiralParticleEvent;
import me.sanjy33.amavyaparticlelib.SpiralParticleEvent;
import org.bukkit.Location;
import org.bukkit.Particle;
import me.sanjy33.amavyaparticlelib.AmavyaParticleLib;

public class AmavyaParticleLibWrapper implements AmavyaParticleLibHook {

    public void addSpiralEffect(Location location, Particle particle, long duration, float length, double radius) {
        AmavyaParticleLib.addParticleEvent(new SpiralParticleEvent(location, particle, duration, length, radius));
    }

    public void addDoubleSpiralEffect(Location location, Particle particle, long duration, float length, double radius) {
        AmavyaParticleLib.addParticleEvent(new DoubleSpiralParticleEvent(location, particle, duration, length, radius));
    }

    public void addBurstEffect(Location location, Particle particle, long duration, float period, double radius, double maxRadius, int steps) {
        AmavyaParticleLib.addParticleEvent(new BurstParticleEvent(location, particle, duration, period, radius, maxRadius, steps));
    }

}
