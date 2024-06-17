package org.example;

import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Config config = Config.Deserialize();
        ClientResponder.getInstance();

        Client client = new Client();
        Thread clientThread = new Thread(client);
        clientThread.start();

        List<Thread> serverThreads = new LinkedList<>();
        for(int port = config.ports.get(0); port <= config.ports.get(1); port++) {
            Server server = new Server(port, client);
            Thread serverThread = new Thread(server);
            serverThread.start();
            serverThreads.add(serverThread);
        }

        try {
            clientThread.join();
            for(Thread thread : serverThreads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}