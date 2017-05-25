package com.tomaszstankowski.gameserver.services;

import com.tomaszstankowski.gameserver.controller.GameController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.UUID;


public class Connection implements Runnable{
    private Socket mSocket;
    private GameController mGameController;
    private static final int SERVER = 100;
    private static final int CLIENT = 200;
    private static final int DATA_SIZE = 32;
    private UUID mId;

    public Connection(Socket socket, GameController gameController, UUID id){
        mSocket = socket;
        mGameController = gameController;
        mId = id;
    }

    @Override
    public void run(){
        try(DataInputStream input = new DataInputStream(mSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(mSocket.getOutputStream()))
        {
            mGameController.printMessage("Connected with a player " + mSocket.getInetAddress());
            initialSend(output);
            while(true){
                try {
                    receive(input);
                    send(output);
                }catch (IOException ioEx){
                    mGameController.printMessage("Connection error with " + mSocket.getInetAddress());
                }
            }
        }
        catch (Exception e){
            mGameController.printMessage("Could not make a connection with " + mSocket.getInetAddress());
            e.printStackTrace();
        }
    }

    private void receive(DataInputStream input) throws IOException{
        while(isClientProcessing(input)){}
        int readSize;
        int offset = 0;
        byte[] data = new byte[DATA_SIZE];
        while((readSize = input.read(data,offset, DATA_SIZE)) != -1){
            offset+=readSize;
        }
        mGameController.updateState(mId, data);
    }

    private void send(DataOutputStream output) throws IOException{
        byte[] data = mGameController.getState();
        notifyClient(output);
        output.write(data);
    }

    private void initialSend(DataOutputStream output) throws IOException{
        byte[] state = mGameController.getState();
        int size = 16 + state.length;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putLong(mId.getLeastSignificantBits());
        buffer.putLong(mId.getMostSignificantBits());
        buffer.put(state);
        notifyClient(output);
        output.write(buffer.array());
    }

    /*When the client finishes reading and processing data, he sets first 4 bytes to fixed value(200).
    * Then server knows its his time to read.
    */
    private boolean isClientProcessing(DataInputStream input) throws IOException{
        input.mark(4);
        boolean val = input.readInt() != CLIENT;
        if(val)
            input.reset();  //go back to beginning  of the input(offset = 0)
        return val;
    }

    /*When server finishes reading and processing data, he sets first 4 bytes to fixed value(100).
    Then the client knows its his time to read.
    * */
    private void notifyClient(DataOutputStream output) throws IOException{
        output.flush();
        byte[] bytes = ByteBuffer.allocate(4).putInt(SERVER).array();
        output.write(bytes);
    }
}
