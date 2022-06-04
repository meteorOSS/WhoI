package com.meteor.whoi.storage;

import com.meteor.whoi.WhoI;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ExceptionColl {

    private WhoI plugin;
    private YamlConfiguration yamlConfiguration;

    public static ExceptionColl getInstance;

    public static void init(WhoI plugin){
        getInstance = new ExceptionColl(plugin);
    }

    private ExceptionColl(WhoI plugin){
        this.plugin = plugin;
        File file = new File(plugin.getDataFolder()+"/error.yml");
        this.yamlConfiguration = file.exists()?YamlConfiguration.loadConfiguration(file):new YamlConfiguration();
    }

    public void collError(String message){
        yamlConfiguration.set(String.valueOf(System.currentTimeMillis()),message);
    }

    public void close(){
        File file = new File(plugin.getDataFolder()+"/error.yml");
        try {
            yamlConfiguration.save(file);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }



}
