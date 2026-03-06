package me.sanjy33.amavyadecoration;

import me.sanjy33.amavyadecoration.hook.AmavyaParticleLibMissing;
import me.sanjy33.amavyadecoration.hook.AmavyaParticleLibHook;
import me.sanjy33.amavyadecoration.hook.AmavyaParticleLibWrapper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class AmavyaDecoration extends JavaPlugin {

    private Listener eventListener;
    public final ShelfManager shelfManager = new ShelfManager(this);
    public final ChiseledBookshelfManager chiseledBookshelfManager = new ChiseledBookshelfManager(this);
    public AmavyaParticleLibHook particleLibHook;

    @Override
    public void onEnable() {
        // Plugin startup logic
        eventListener = new PlayerListener(this);

        setupAmavyaParticleLib();

        getServer().getPluginManager().registerEvents(eventListener, this);
        load();
        shelfManager.refreshDisplays();
        chiseledBookshelfManager.startBookshelfTask();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        shelfManager.cleanupDisplays();
        chiseledBookshelfManager.cleanup();
        save();
    }

    private void load() {
        File file = new File(getDataFolder(), "data.yml");
        YamlConfiguration baseSection = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection shelfSection = baseSection.getConfigurationSection("shelves");
        shelfManager.load(shelfSection);
    }

    public void saveAsync() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            save();
        }, 1L);
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection shelfSection = config.createSection("shelves");
        shelfManager.save(shelfSection);
        File file = new File(getDataFolder(), "data.yml");
        try {
            config.save(file);
        } catch (IOException e) {
            getLogger().info("Error saving data! " + e.getMessage());
        }
    }

    private void setupAmavyaParticleLib() {
        if (Bukkit.getPluginManager().getPlugin("AmavyaParticleLib") != null) {
            particleLibHook = new AmavyaParticleLibWrapper();
            getLogger().info("Particle effects enabled!");
        }else{
            particleLibHook = new AmavyaParticleLibMissing();
            getLogger().info("AmavyaParticleLib not found. Particle effects disabled!");
        }
    }
}
