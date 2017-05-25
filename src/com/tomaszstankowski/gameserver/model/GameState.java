package com.tomaszstankowski.gameserver.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameState {
    private Map<UUID,PlayerState> states = new HashMap<>();

    public synchronized void savePlayerState(PlayerState newState){
        states.put(newState.id, newState);
    }

    public synchronized void reducePlayerHp(UUID playerId, short damage){
        short hp = states.get(playerId).hp;
        hp -= damage;
        if(hp < 0)
            hp =0;
        states.get(playerId).hp = hp;
    }

    public PlayerState[] getGameState(){
        return states.values().toArray(new PlayerState[states.size()]);
    }
}
