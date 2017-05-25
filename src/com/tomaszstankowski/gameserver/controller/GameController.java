package com.tomaszstankowski.gameserver.controller;


import com.tomaszstankowski.gameserver.Server;
import com.tomaszstankowski.gameserver.model.GameState;
import com.tomaszstankowski.gameserver.model.PlayerState;
import com.tomaszstankowski.gameserver.services.Connection;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameController {
    private static final int MAX_PLAYERS = 4;
    private ExecutorService executorService = Executors.newFixedThreadPool(MAX_PLAYERS);
    private List<Connection> mActiveConnections = new ArrayList<>();
    private GameState mGameState = new GameState();
    private Server mServer;

    public GameController(Server server){
        mServer = server;
    }

    public void printMessage(String message){
        mServer.printMessage(message);
    }

    public void onConnect(Socket socket){
        if(mActiveConnections.size() <= MAX_PLAYERS) {
            UUID playerId = UUID.randomUUID();
            mGameState.savePlayerState(new PlayerState(playerId));
            Connection c = new Connection(socket, this, playerId);
            mActiveConnections.add(c);
            executorService.execute(c);
        }
        else{
            try {
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            finally {
                mServer.printMessage("Limit for players is " + MAX_PLAYERS);
            }
        }
    }

    public void updateState(UUID sender, byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        PlayerState newState = new PlayerState(sender);
        newState.x = buffer.getFloat();
        newState.y = buffer.getFloat();
        newState.z = buffer.getFloat();
        newState.hp = buffer.getShort();
        mGameState.savePlayerState(newState);
        /*if sending player did not hit anybody, his client sends empty bytes otherwise
        he sends id of player who was hit(16bytes) and damage dealt(2bytes)*/
        byte[] id = new byte[16];
        buffer.get(id,14,16);
        boolean isEmpty = true;
        for(int i=0;i<16;i++)
            if(id[i] != (byte)0) {
                isEmpty = false;
                break;
            }
        if(!isEmpty) {
            UUID hitId = UUID.nameUUIDFromBytes(id);
            short dmg = buffer.getShort();
            mGameState.reducePlayerHp(hitId, dmg);
        }
    }

    public byte[] getState(){
        PlayerState[] gameState = mGameState.getGameState();
        int count = gameState.length;
        int singleSize = 16 + 3 * 4 + 2;    //id + position + hp
        int size = singleSize * count;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(count);
        for(PlayerState s : gameState){
            buffer.putLong(s.id.getLeastSignificantBits());
            buffer.putLong(s.id.getMostSignificantBits());
            buffer.putFloat(s.x);
            buffer.putFloat(s.y);
            buffer.putFloat(s.z);
            buffer.putShort(s.hp);
        }
        return buffer.array();
    }
}
