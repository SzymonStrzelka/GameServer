package com.tomaszstankowski.gameserver;

import com.tomaszstankowski.gameserver.controller.GameController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private static final int PORT = 2137;
    private GameController gameController = new GameController(this);

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    public void start(){
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            printMessage("Waiting for players");
            while (true) {
                Socket socket = serverSocket.accept();
                gameController.onConnect(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred. Game could not be hosted.");
        }
    }

    public void printMessage(String message){
        System.out.println(message);
    }
}
