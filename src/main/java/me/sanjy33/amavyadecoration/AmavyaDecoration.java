package me.sanjy33.amavyadecoration;

import io.papermc.paper.command.brigadier.BasicCommand;
import me.sanjy33.amavyadecoration.command.ReloadCommand;
import me.sanjy33.amavyadecoration.hook.AmavyaParticleLibMissing;
import me.sanjy33.amavyadecoration.hook.AmavyaParticleLibHook;
import me.sanjy33.amavyadecoration.hook.AmavyaParticleLibWrapper;
import me.sanjy33.amavyadecoration.listener.AutoFeedingListener;
import me.sanjy33.amavyadecoration.listener.ShelfEventListener;
import me.sanjy33.amavyadecoration.manager.AmavyaDecorationManager;
import me.sanjy33.amavyadecoration.manager.AutoFeedingManager;
import me.sanjy33.amavyadecoration.manager.ChiseledBookshelfManager;
import me.sanjy33.amavyadecoration.manager.ShelfManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class AmavyaDecoration extends JavaPlugin {

    public final List<AmavyaDecorationManager> managers = new ArrayList<>();
    public final ShelfManager shelfManager = new ShelfManager(this);
    public final ChiseledBookshelfManager chiseledBookshelfManager = new ChiseledBookshelfManager(this);
    public final AutoFeedingManager autoFeedingManager = new AutoFeedingManager(this);
    public AmavyaParticleLibHook particleLibHook;

    @Override
    public void onEnable() {
        // Plugin startup logic

        setupAmavyaParticleLib();

        BasicCommand reloadCommand = new ReloadCommand(this);
        registerCommand("adreload", reloadCommand);

        getServer().getPluginManager().registerEvents(new ShelfEventListener(this), this);
        getServer().getPluginManager().registerEvents(new AutoFeedingListener(autoFeedingManager), this);
        saveDefaultConfig();
        load();
        reloadManagers();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (AmavyaDecorationManager manager :  managers) {
            manager.cleanup();
        }
        save();
    }

    public void reloadManagers() {
        reloadConfig();
        for (AmavyaDecorationManager manager : managers) {
            manager.reload();
            if (manager.isEnabled()) {
                getLogger().info(manager.getName() + " is enabled.");
            } else {
                getLogger().info(manager.getName() + " is disabled.");
            }
        }
    }

    private void load() {
        File file = new File(getDataFolder(), "data.yml");
        YamlConfiguration baseSection = YamlConfiguration.loadConfiguration(file);
        for (AmavyaDecorationManager manager : managers) {
            manager.loadData(baseSection);
        }
    }

    public void saveAsync() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, this::save, 1L);
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (AmavyaDecorationManager manager : managers) {
            manager.saveData(config);
        }
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
