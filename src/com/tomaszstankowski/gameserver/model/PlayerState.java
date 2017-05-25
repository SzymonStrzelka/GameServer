package com.tomaszstankowski.gameserver.model;


import java.util.UUID;

public class PlayerState {
    public UUID id;
    public float x;
    public float y;
    public float z;
    public short hp = 100;

    public PlayerState(UUID id){
        this.id = id;
    }
}
