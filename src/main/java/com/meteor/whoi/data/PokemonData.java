package com.meteor.whoi.data;

import org.bukkit.map.MapRenderer;

public class PokemonData {
    int pokemonId;
    private long createTime;
    private long broStartTime;
    private boolean alreadyOpen;

    public PokemonData(int pokemonId, long createTime, long broStartTime, boolean alreadyOpen) {
        this.pokemonId = pokemonId;
        this.createTime = createTime;
        this.broStartTime = broStartTime;
        this.alreadyOpen = alreadyOpen;
    }

    public int getPokemonId() {
        return pokemonId;
    }

    public void setPokemonId(int pokemonId) {
        this.pokemonId = pokemonId;
    }


    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getBroStartTime() {
        return broStartTime;
    }

    public void setBroStartTime(long broStartTime) {
        this.broStartTime = broStartTime;
    }

    public boolean isAlreadyOpen() {
        return alreadyOpen;
    }

    public void setAlreadyOpen(boolean alreadyOpen) {
        this.alreadyOpen = alreadyOpen;
    }
}
