package org.example;

import java.util.ArrayList;
import java.util.List;

public class ClientResponder {
    private static ClientResponder instance = null;
    private final List<ServerConnection> connections;

    public void putMessage(int connectionId, String message) {
        ServerConnection connection = connections.stream().filter(t -> t.hashCode() == connectionId).findFirst().orElse(null);
        if (connection != null) {
            Event event = new Event(EventType.EOutputReady, message);
            connection.putEvent(event);
        }
    }

    public void registerConnection(ServerConnection connection) {
        connections.add(connection);
    }

    ClientResponder() {
        connections = new ArrayList<>();
    }

    public static ClientResponder getInstance() {
        if (instance == null) {
            instance = new ClientResponder();
        }
        return instance;
    }
}
