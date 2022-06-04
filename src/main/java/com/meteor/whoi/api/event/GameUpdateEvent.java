package com.meteor.whoi.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameUpdateEvent extends Event {
    private static  final HandlerList handlerList = new HandlerList();

    private Player player;

    public GameUpdateEvent(Player player){
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }


    public static HandlerList getHandlerList() {
        return handlerList;
    }


    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
