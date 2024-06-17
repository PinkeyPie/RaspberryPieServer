package org.example;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Client client;


    public int port;
    boolean running;

    Server(int port, Client client) {
        this.port = port;
        running = true;
        this.client = client;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (running) {
                clientSocket = serverSocket.accept();
                Thread thread = new Thread(new ServerConnection(clientSocket, port, client));
                thread.start();
            }
        } catch (IOException exception) {
            System.out.println("Server connection error: " + exception.getMessage());
        }
    }

    public void stop() throws IOException {
        running = false;
        clientSocket.close();
        serverSocket.close();
    }
}
