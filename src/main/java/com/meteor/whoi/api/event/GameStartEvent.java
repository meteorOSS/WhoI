package com.meteor.whoi.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStartEvent extends Event {
    private static  final HandlerList handlerList = new HandlerList();

    private String pokemon;

    public GameStartEvent(String pokemon){
        this.pokemon = pokemon;
    }

    public String getPokemon() {
        return pokemon;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }


    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

}
