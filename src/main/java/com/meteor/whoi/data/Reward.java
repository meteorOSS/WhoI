package com.meteor.whoi.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class Reward {
    private int chance;
    private List<String> cmds;
    public Reward(ConfigurationSection configurationSection){
        this.chance = configurationSection.getInt("chance");
        this.cmds = configurationSection.getStringList("cmds");
    }

    public int getChance() {
        return chance;
    }

    public List<String> getCmds() {
        return cmds;
    }
}
