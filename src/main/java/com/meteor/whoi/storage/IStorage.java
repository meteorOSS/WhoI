package com.meteor.whoi.storage;

public interface IStorage {

    String getPokemon(int key);
    int getPokemonIndex(String key);
    boolean initPokemonFile(int id);
    void doGetPokemonIndex();
    String getTrPokemonIndex(int key);
    void close();
}
