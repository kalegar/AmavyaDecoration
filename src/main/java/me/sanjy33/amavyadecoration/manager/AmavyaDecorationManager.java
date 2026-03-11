package me.sanjy33.amavyadecoration.manager;

import org.bukkit.configuration.file.YamlConfiguration;

public interface AmavyaDecorationManager {

    String getName();
    boolean isEnabled();
    void reload();
    void cleanup();
    void loadData(YamlConfiguration config);
    void saveData(YamlConfiguration config);

}
