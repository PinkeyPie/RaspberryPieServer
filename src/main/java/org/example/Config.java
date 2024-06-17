package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Config implements Serializable {
    public List<Integer> ports;
    public Config() {
        ports = new ArrayList<>();
    }
    public void Serialize() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    public static Config Deserialize() {
        ObjectMapper mapper = new ObjectMapper();
        Config config = null;
        try {
            System.out.println(System.getProperty("user.dir"));
            config = mapper.readValue(new File("config.json"), Config.class);
        } catch (IOException e) {
            System.out.println("No such file config.json");
        }
        return config;
    }
}
