package org.example;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class ServerConnection implements Runnable {
    private final Socket socket;
    private static final int increaseSize = 1024;
    private final Client client;
    Queue<Event> events;
    private StringBuilder currentXml;
    private int port;

    public ServerConnection(Socket socket, int port, Client client) {
        this.socket = socket;
        this.client = client;
        events = new LinkedList<>();
        currentXml = new StringBuilder();
        ClientResponder.getInstance().registerConnection(this);
        this.port = port;
    }

    Document SuccessParsed(String data) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(data)));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            return null;
        }
    }

    private class InputHandler implements Runnable {
        private char[] bytes;
        private int length;
        private final ServerConnection connection;
        private static final int increaseSize = 1024;

        void increaseSizeBuffer() {
            char[] tempBytes = Arrays.copyOf(bytes, length);
            bytes = new char[length + increaseSize];
            System.arraycopy(tempBytes, 0, bytes, 0, length);
        }
        @Override
        public void run() {
            int key = 0;
            try (InputStream in = socket.getInputStream()) {
                while (key != -1) {
                    key = in.read();
                    bytes[length] = (char)key;
                    if(bytes[length] == '>') {
                        char[] tempBytes = Arrays.copyOf(bytes, length + 1);
                        String input = new String(tempBytes);
                        Event event = new Event(EventType.EInputReady, input);
                        connection.putEvent(event);
                        bytes = new char[increaseSize];
                        length = 0;
                        continue;
                    }
                    length++;
                    if(bytes.length == length) {
                        increaseSizeBuffer();
                    }
                }
                Event event = new Event(EventType.EConnectionClose, "");
                connection.putEvent(event);
            } catch (IOException exception) {
                System.out.println("Error while reading from socket: " + exception.getMessage());
            }
        }
        public InputHandler(Socket socket, ServerConnection connection) {
            this.connection = connection;
            bytes = new char[increaseSize];
        }
    }

    private synchronized Event getEvent() {
        try {
            while (events.isEmpty()) {
                wait();
            }
            return events.poll();
        } catch (InterruptedException exception) {
            System.out.println("Error while await: " + exception.getMessage());
            return null;
        }
    }

    public synchronized void putEvent(Event event) {
        events.add(event);
        notifyAll();
    }

    @Override
    public void run() {
        InputHandler inputHandler = new InputHandler(socket, this);
        Thread thread = new Thread(inputHandler);
        thread.start();

        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            while (!socket.isClosed()) {
                Event event = getEvent();
                if (event != null) {
                    if (event.getType() == EventType.EOutputReady) {
                        String output = event.getData();
                        out.println(output);
                    } else if (event.getType() == EventType.EInputReady) {
                        String input = event.getData();
                        currentXml.append(input);
                        Document doc = SuccessParsed(input);
                        if(doc != null) {
                            currentXml = new StringBuilder();
                            client.putDocument(hashCode(), port, doc);
                            System.gc();
                        }
                    } else if(event.getType() == EventType.EConnectionClose) {
                        client.putDocument(hashCode(), port, null);
                        socket.close();
                    }
                }
            }
            System.out.println("Server connection closed");
        } catch (IOException exception) {
            System.out.println("Error while reading from socket: " + exception.getMessage());
        }
    }
}
