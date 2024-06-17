package org.example;

public class Event {
    private final EventType type;
    private final String data;
    public Event(EventType type, String data) {
        this.type = type;
        this.data = data;
    }
    public EventType getType() {
        return type;
    }
    public String getData() {
        return data;
    }
}
