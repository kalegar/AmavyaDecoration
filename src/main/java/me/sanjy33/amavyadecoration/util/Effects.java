package me.sanjy33.amavyadecoration.util;

import me.sanjy33.amavyadecoration.AmavyaDecoration;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

public class Effects {

    public static void playEatingEffect(AmavyaDecoration plugin, LivingEntity entity) {
        Effects.playEatingEffect(plugin, entity, ItemStack.of(Material.WHEAT));
    }

    public static void playEatingEffect(AmavyaDecoration plugin, LivingEntity entity, ItemStack item) {

        if (entity instanceof Sheep) {
            entity.playEffect(EntityEffect.SHEEP_EAT_GRASS);
        }

        Integer[] delays = new Integer[] {1, 6, 11};
        for (int delay : delays) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.particleLibHook.addBurstEffect(entity.getEyeLocation(), Particle.ITEM, 10, 1, 0, 0.5, 8, item);
                entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
            }, delay);
        }
    }

}
