package org.hassan.fadedminions.files;

import com.google.common.base.Charsets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hassan.fadedminions.FadedMinions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MinionsConfig {

    private File file;
    private static String FILE_NAME = "Minions.yml";
    private YamlConfiguration configuration;

    private FadedMinions plugin;

    public MinionsConfig(FadedMinions plugin) {
        this.plugin = plugin;

        file = new File(plugin.getDataFolder(), FILE_NAME);

        if(!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }

        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            this.configuration.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        this.configuration = YamlConfiguration.loadConfiguration(file);

        InputStream defConfigStream = plugin.getResource(FILE_NAME + ".yml");
        if(defConfigStream == null) {
            return;
        }

        this.configuration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }

    public YamlConfiguration getConfiguration(){
        return configuration;
    }




}

