package me.sanjy33.amavyadecoration.manager;

import com.destroystokyo.paper.entity.Pathfinder;
import me.sanjy33.amavyadecoration.AmavyaDecoration;
import me.sanjy33.amavyadecoration.util.LocationKey;
import me.sanjy33.amavyadecoration.util.Effects;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class AutoFeedingManager implements AmavyaDecorationManager {

    private final AmavyaDecoration plugin;
    private final Map<String, Integer> feedingBlocks = new HashMap<>();
    private final NamespacedKey namespacedKeyLocation;
    private final NamespacedKey namespacedKeyCooldown;
    private BukkitTask task;
    private boolean enabled = false;
    private final int maxHayCount = 9;
    private final Random rand = new Random();
    private static final EntityType[] validEntities = new EntityType[] {EntityType.COW, EntityType.SHEEP};

    public AutoFeedingManager(AmavyaDecoration plugin) {
        this.plugin = plugin;
        this.plugin.managers.add(this);
        this.namespacedKeyLocation = new NamespacedKey(plugin, "auto_feeder_location");
        this.namespacedKeyCooldown = new NamespacedKey(plugin, "auto_feeder_cooldown");
    }

    public boolean isEntityValid(Entity entity) {
        boolean valid = false;
        for (EntityType entityType : validEntities) {
            if (entity.getType().equals(entityType)) {
                valid = true;
                break;
            }
        }
        valid = valid && (entity instanceof Animals);
        valid = valid && ((Animals) entity).isAdult();
        valid = valid && !((Animals) entity).isLoveMode();
        valid = valid && ((Animals) entity).canBreed();
        return valid;
    }

    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void startTask() {
        stopTask();
        if (!enabled) return;
        final int radius = plugin.getConfig().getInt("auto_animal_feeding.radius", 12);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<String, Integer>> iterator = feedingBlocks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> entry = iterator.next();
                Location location = LocationKey.stringToLocation(entry.getKey());
                if (!location.getBlock().getType().equals(Material.HAY_BLOCK)) {
                    iterator.remove();
                    continue;
                }
                Collection<LivingEntity> entities = location.getNearbyLivingEntities(radius);
                for (LivingEntity entity : entities) {
                    pathAnimalToFeedingBlock(entity, location);
                }
            }
        }, plugin.getConfig().getLong("auto_animal_feeding.task.delay", 20), plugin.getConfig().getLong("auto_animal_feeding.task.period", 200));
    }

    @Override
    public String getName() {
        return "Automatic Animal Feeding";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void reload() {
        stopTask();
        enabled = plugin.getConfig().getBoolean("auto_animal_feeding.enabled", true);
        if (enabled) {
            startTask();
        }
    }

    @Override
    public void cleanup() {
        stopTask();
    }

    @Override
    public void loadData(YamlConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("auto_feeding");
        if (section == null) return;
        if (section.contains("hay_blocks")) {
            Map<String, Object> map = section.getConfigurationSection("hay_blocks").getValues(false);
            feedingBlocks.clear();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                feedingBlocks.put(entry.getKey(), (Integer) entry.getValue());
            }
        }
    }

    @Override
    public void saveData(YamlConfiguration config) {
        ConfigurationSection section = config.createSection("auto_feeding");
        section.createSection("hay_blocks", feedingBlocks);
    }

    public void onFeedingBlockPlace(Block block) {
        if (!block.getType().equals(Material.HAY_BLOCK)) return;
        feedingBlocks.put(LocationKey.locationToString(block.getLocation()), maxHayCount);
    }

    public void onAnimalPathFind(Entity entity) {
        if (!isEntityValid(entity)) {
            return;
        }
        Animals animal = (Animals) entity;
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        String locString = pdc.getOrDefault(this.namespacedKeyLocation, PersistentDataType.STRING, "");
        if (locString.isEmpty()) {
            return;
        }
        if (feedingBlocks.containsKey(locString)) {
            int damage = feedingBlocks.get(locString);
            if (damage <= 0) {
                return;
            }
            Location location = LocationKey.stringToLocation(locString);
            double dist = animal.getLocation().distanceSquared(location);
            if (dist < 2.5) {
                Effects.playEatingEffect(plugin, animal);
                // Set mob to breed mode after effect finishes
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    animal.playEffect(EntityEffect.LOVE_HEARTS);
                    animal.setLoveModeTicks(600);
                }, 12);
                animal.getPersistentDataContainer().set(this.namespacedKeyCooldown, PersistentDataType.LONG, System.currentTimeMillis()+plugin.getConfig().getLong("auto_animal_feeding.cooldownms", 30000));
                animal.getPersistentDataContainer().remove(this.namespacedKeyLocation);

                damage -= 1;
                if (damage > 0) {
                    feedingBlocks.put(locString, damage);
                } else {
                    location.getBlock().setType(Material.AIR);
                    plugin.particleLibHook.addBurstEffect(location, Particle.ITEM, 10, 1, 0, 1, 8, ItemStack.of(Material.WHEAT));
                }
            }
        }
    }

    public ItemStack onFeedingBlockDrop(Location location) {
        Block hayBlock = location.getBlock();
        if (!hayBlock.getType().equals(Material.HAY_BLOCK)) return null;

        String key = LocationKey.locationToString(location);
        int damage = 0;
        if (feedingBlocks.containsKey(key)) {
            damage = feedingBlocks.get(key);
        }
        if (damage > 0 && damage < maxHayCount) {
            return ItemStack.of(Material.WHEAT, damage);
        } else {
            return ItemStack.of(Material.HAY_BLOCK);
        }
    }

    public Location getRandomLocation(Location center, double minRadius, double radius) {
        double x, y, z;
        x = rand.nextDouble(minRadius,radius) * (rand.nextInt(2)*-1);
        y = rand.nextDouble(minRadius,radius) * (rand.nextInt(2)*-1);
        z = rand.nextDouble(minRadius,radius) * (rand.nextInt(2)*-1);
        Location location = center.clone().add(x,y,z);
        Block block = location.getBlock();
        while (!(block.isEmpty() || block.isPassable())) {
            block = block.getRelative(BlockFace.UP);
            if (block.getY() >= block.getWorld().getMaxHeight()-1) break;
        }
        return block.getLocation();
    }

    public void pathAnimalToFeedingBlock(Entity entity, Location location) {
        // Only adult cows and sheep should move towards hay blocks.
        if (!isEntityValid(entity)) {
            return;
        }
        Animals animal = (Animals) entity;
        if (entity.getPersistentDataContainer().has(this.namespacedKeyCooldown)) {
            Long expiry = entity.getPersistentDataContainer().getOrDefault(this.namespacedKeyCooldown, PersistentDataType.LONG, 0L);
            if (System.currentTimeMillis() < expiry) {
                return;
            } else {
                entity.getPersistentDataContainer().remove(this.namespacedKeyCooldown);
            }
        }
        Location destination = location;
        // If already moving towards a hay bale, keep that destination. Unless the haybale no longer exists.
        if (animal.getPersistentDataContainer().has(this.namespacedKeyLocation)) {
            String locString = animal.getPersistentDataContainer().getOrDefault(this.namespacedKeyLocation, PersistentDataType.STRING, "");
            if (!locString.isEmpty()) {
                Location currentDest = LocationKey.stringToLocation(locString);
                if (currentDest.getBlock().getType().equals(Material.HAY_BLOCK)) {
                    destination = currentDest;
                }
            }
        }
        Pathfinder.PathResult result = animal.getPathfinder().findPath(destination);
        if (result != null && result.canReachFinalPoint() && result.getFinalPoint() != null) {
            animal.getPathfinder().moveTo(result.getFinalPoint());
            entity.getPersistentDataContainer().set(this.namespacedKeyLocation, PersistentDataType.STRING, LocationKey.locationToString(destination));
        }
    }
    // Maybe add compost bin for pigs
    // Craft goggle synced up to ore type, make that ore block glow

}
