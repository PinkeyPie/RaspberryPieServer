package org.example;

import java.util.Date;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.Queue;
import org.w3c.dom.*;

public class Client implements Runnable {
    private class Data {
        public int connectionId;
        public int port;
        public Document document;

        public Data(int connectionId, int port, Document document) {
            this.connectionId = connectionId;
            this.port = port;
            this.document = document;
        }
    }

    Queue<Data> documents;

    public synchronized void putDocument(int clientId, int port, Document document) {
        Data data = new Data(clientId, port, document);
        documents.add(data);
        notifyAll();
    }

    public Client() {
        documents = new LinkedList<Data>();
    }

    public synchronized Data getDocument() {
        try {
            while (documents.isEmpty()) {
                wait();
            }
            Data document = documents.poll();
            return document;
        } catch (InterruptedException exception) {
            return null;
        }
    }

    @Override
    public void run() {
        while (true) {
            Data document = getDocument();
            if(document.document == null) {
                System.out.println(new Formatter().format("Pidor on port: %d disconnected", document.port).toString());
            } else {
                System.out.println(document.document);
            }
            System.out.println("Looks nice!");
            ClientResponder.getInstance().putMessage(document.connectionId, "Pidor");
        }
    }
}
