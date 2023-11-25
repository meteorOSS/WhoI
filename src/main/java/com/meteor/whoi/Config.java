package com.meteor.whoi;

import com.meteor.whoi.data.Reward;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Config{
    private WhoI plugin;
    private MessageManager messageManager;
    private List<Reward> rewardList;
    public static Config config;

    public static void init(WhoI plugin){
        config = new Config(plugin);
    }

    private Config(WhoI plugin){
        this.plugin = plugin;
        this.reload();
    }


    public void reload(){
        Arrays.asList("message.yml").forEach(name->{
            File file = new File(plugin.getDataFolder()+"/"+name);
            if(!file.exists())
                plugin.saveResource(name,false);
        });
        File file = new File(plugin.getDataFolder()+"/image/");
        if(!file.exists())
            file.mkdir();
        plugin.reloadConfig();
        this.rewardList = new ArrayList<>();
        ConfigurationSection configurationSection = plugin.getConfig().getConfigurationSection("rewards");
        configurationSection.getKeys(false).forEach(s->rewardList.add(new Reward(configurationSection.getConfigurationSection(s))));
        messageManager = new MessageManager(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()+"/message.yml")),true);
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public Reward lottery(){
        List<Reward> rewards = rewardList;
        if(rewards==null||rewards.isEmpty()){
            return null;
        }
        int size = rewards.size();
        double sumChance = 0d;
        for(Reward reward : rewards){
            sumChance+=reward.getChance();
        }
        List<Double> sortOrginRates = new ArrayList<Double>(size);
        Double temp = 0d;
        for (Reward reward : rewards){
            temp += reward.getChance();
            sortOrginRates.add(temp/sumChance);
        }
        //区块值获取物品索引
        double nextDouble = Math.random();
        sortOrginRates.add(nextDouble);
        Collections.sort(sortOrginRates);
        int num = sortOrginRates.indexOf(nextDouble);
        return rewards.get(num);
    }
}
