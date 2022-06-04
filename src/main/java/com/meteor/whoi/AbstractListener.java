package com.meteor.whoi;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class AbstractListener<P extends Plugin> implements Listener {
    P plugin;

    public P getPlugin() {
        return plugin;
    }

    public AbstractListener(P plugin){
        this.plugin = plugin;
    }
    public void register(){
        this.plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }
    public void unRegister(){
        HandlerList.unregisterAll((Listener)this);
    }
}