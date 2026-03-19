package me.sanjy33.amavyadecoration.manager;

import me.sanjy33.amavyadecoration.AmavyaDecoration;
import me.sanjy33.amavyadecoration.flags.AnimatedFlag;
import me.sanjy33.amavyadecoration.flags.FlagSetup;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class FlagManager implements AmavyaDecorationManager {

    private final AmavyaDecoration plugin;
    private boolean enabled = true;
    private final Map<UUID, AnimatedFlag> flags = new HashMap<>();
    private BukkitTask animateTask = null;
    private final Set<Material> allowedFlagMaterials = new HashSet<>();
    private final Set<Material> allowedFlagPoleMaterials = new HashSet<>();
    private final Map<UUID, FlagSetup> flagSetups = new HashMap<>();
    private float windSpeed = 0.25f;
    private float windSpeedMin = 0.01f;
    private float windSpeedMax = 0.5f;
    private float windSpeedTarget = windSpeed;
    private final Random rand = new Random();
    private final NamespacedKey entityKey;

    private static final Material[] DEFAULT_FLAG_MATERIALS = new Material[] {
            Material.BLACK_WOOL,
            Material.WHITE_WOOL,
            Material.BLUE_WOOL,
            Material.CYAN_WOOL,
            Material.BROWN_WOOL,
            Material.GRAY_WOOL,
            Material.ORANGE_WOOL,
            Material.PINK_WOOL,
            Material.RED_WOOL,
            Material.GREEN_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.LIGHT_GRAY_WOOL,
            Material.LIME_WOOL,
            Material.MAGENTA_WOOL,
            Material.PURPLE_WOOL,
            Material.YELLOW_WOOL
    };

    private static final Material[] DEFAULT_FLAGPOLE_MATERIALS = new Material[] {
        Material.ACACIA_FENCE,
        Material.BAMBOO_FENCE,
        Material.BIRCH_FENCE,
        Material.CHERRY_FENCE,
        Material.CRIMSON_FENCE,
        Material.DARK_OAK_FENCE,
        Material.JUNGLE_FENCE,
        Material.MANGROVE_FENCE,
        Material.NETHER_BRICK_FENCE,
        Material.OAK_FENCE,
        Material.PALE_OAK_FENCE,
        Material.SPRUCE_FENCE,
        Material.WARPED_FENCE,
        Material.ANDESITE_WALL,
        Material.BLACKSTONE_WALL,
        Material.BRICK_WALL,
        Material.COBBLESTONE_WALL,
        Material.COBBLED_DEEPSLATE_WALL,
        Material.DEEPSLATE_BRICK_WALL,
        Material.DEEPSLATE_TILE_WALL,
        Material.DIORITE_WALL,
        Material.END_STONE_BRICK_WALL,
        Material.GRANITE_WALL,
        Material.MOSSY_COBBLESTONE_WALL,
        Material.MOSSY_STONE_BRICK_WALL,
        Material.MUD_BRICK_WALL,
        Material.NETHER_BRICK_WALL,
        Material.POLISHED_BLACKSTONE_BRICK_WALL,
        Material.POLISHED_BLACKSTONE_WALL,
        Material.POLISHED_DEEPSLATE_WALL,
        Material.POLISHED_TUFF_WALL,
        Material.PRISMARINE_WALL,
        Material.RED_NETHER_BRICK_WALL,
        Material.RED_SANDSTONE_WALL,
        Material.RESIN_BRICK_WALL,
        Material.SANDSTONE_WALL,
        Material.TUFF_WALL,
        Material.TUFF_BRICK_WALL
    };

    public FlagManager(AmavyaDecoration plugin) {
        this.plugin = plugin;
        this.plugin.managers.add(this);
        this.entityKey = new NamespacedKey(plugin, "flag_entity");
    }

    public NamespacedKey getEntityKey() {
        return entityKey;
    }

    public void addFlag(AnimatedFlag flag) {
        this.flags.put(flag.getId(), flag);
    }

    public FlagSetup startFlagSetup(Player player) {
        FlagSetup setup = new FlagSetup(player);
        flagSetups.put(player.getUniqueId(),setup);
        return setup;
    }

    public FlagSetup getFlagSetup(Player player) {
        return flagSetups.get(player.getUniqueId());
    }

    public void removeFlagSetup(Player player) {
        this.flagSetups.remove(player.getUniqueId());
    }

    public AnimatedFlag getFlagByID(UUID id) {
        return flags.get(id);
    }

    public void stopAnimateTask() {
        if (this.animateTask != null && !this.animateTask.isCancelled()) {
            this.animateTask.cancel();
            this.animateTask = null;
        }
    }

    public void startAnimateTask() {
        stopAnimateTask();
        this.animateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (Math.abs(windSpeed - windSpeedTarget) < 0.001f) {
                windSpeedTarget = rand.nextFloat(windSpeedMin, windSpeedMax);
            } else {
                windSpeed += (windSpeedTarget - windSpeed) * 0.05f;
            }
            for (Iterator<Map.Entry<UUID, AnimatedFlag>> it = flags.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<UUID, AnimatedFlag> entry = it.next();
                AnimatedFlag flag = entry.getValue();
                if (!flag.isValid()) {
                    it.remove();
                    continue;
                }
                if (!flag.isChunkLoaded()) {
                    continue;
                }
                if (!flag.hasFlagPole()) {
                    flag.detectFlagPole();
                    if (!flag.isValid()) {
                        it.remove();
                        continue;
                    }
                }
                flag.animate(windSpeed);
            }
        }, 100, 1);
        plugin.getLogger().info("Flag animate task started");
    }

    public Set<Material> getAllowedFlagMaterials() {
        return allowedFlagMaterials;
    }

    public boolean isFlagMaterialAllowed(Material material) {
        return allowedFlagMaterials.contains(material);
    }

    public boolean isPoleMaterialAllowed(Material material) {
        return allowedFlagPoleMaterials.contains(material);
    }

    @Override
    public String getName() {
        return "Flags";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void reload() {
        stopAnimateTask();
        this.enabled = plugin.getConfig().getBoolean("flags.enabled", true);
        this.windSpeedMin = (float) plugin.getConfig().getDouble("flags.windspeed.min", 0.01);
        this.windSpeedMax = (float) plugin.getConfig().getDouble("flags.windspeed.max", 0.5);
        this.allowedFlagMaterials.clear();
        this.allowedFlagPoleMaterials.clear();
        if (plugin.getConfig().contains("flags.materials")) {
            List<String> materialNames = plugin.getConfig().getStringList("flags.materials");
            for (String name : materialNames) {
                try {
                    Material material = Material.valueOf(name.trim().replaceAll(" ","_").toUpperCase());
                    this.allowedFlagMaterials.add(material);
                } catch (Exception e) {
                    // Ignore
                }
            }
        } else {
            this.allowedFlagMaterials.addAll(Arrays.asList(DEFAULT_FLAG_MATERIALS));
        }
        if (plugin.getConfig().contains("flags.polematerials")) {
            List<String> materialNames = plugin.getConfig().getStringList("flags.polematerials");
            for (String name : materialNames) {
                try {
                    Material material = Material.valueOf(name.trim().replaceAll(" ", "_").toUpperCase());
                    this.allowedFlagPoleMaterials.add(material);
                } catch (Exception e) {
                    // Ignore
                }
            }
        } else {
            this.allowedFlagPoleMaterials.addAll(Arrays.asList(DEFAULT_FLAGPOLE_MATERIALS));
        }
        if (this.enabled) {
            startAnimateTask();
        }
    }

    @Override
    public void cleanup() {
        for (AnimatedFlag flag : flags.values()) {
            flag.cleanup();
        }
    }

    @Override
    public void loadData(YamlConfiguration config) {
        flags.clear();
        ConfigurationSection flagSection = config.getConfigurationSection("flags");
        if (flagSection != null) {
            Set<String> sectionKeys = flagSection.getKeys(false);
            for (String key : sectionKeys) {
                ConfigurationSection section = flagSection.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                AnimatedFlag flag = new AnimatedFlag(this, UUID.fromString(key), section);
                flags.put(flag.getId(), flag);
            }
        }
        plugin.getLogger().info("Loaded " + flags.size() + " animated flags.");
    }

    @Override
    public void saveData(YamlConfiguration config) {
        ConfigurationSection flagSection = config.createSection("flags");
        for (AnimatedFlag flag : flags.values()) {
            ConfigurationSection section = flagSection.createSection(flag.getId().toString());
            flag.toConfig(section);
        }
    }
}
