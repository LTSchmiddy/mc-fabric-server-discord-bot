package net.lt_schmiddy.serverdiscordbot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigHandler {
    public static final File configFile = new File("config/server-discord-bot.json");
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static Config config;

    // public ConfigHandler(){}


    public static void save(){
        String json = gson.toJson(config);
        try {
            FileOutputStream stream = new FileOutputStream(configFile);
            stream.write(json.getBytes(StandardCharsets.UTF_8));
            stream.close();
        } catch (IOException e) {
            System.out.println("Error occurred while saving config file: " + e.getMessage());
        }
    }



    public static Config load(){
        if (configFile.exists()) {
            try {
                FileReader reader = new FileReader(configFile);
                config =  gson.fromJson(reader, Config.class);
            } catch (IOException e) {
                System.out.println("Error occurred while loading config file " + e.getMessage());
                config = new Config();
            }
        } else {
            config = create();
        }

        return config;
    }

    public static Config create() {
        config = new Config();
        try {
            // new File("config/SmithAndFletch/").mkdirs();
            FileOutputStream stream = new FileOutputStream(configFile);
            String json = gson.toJson(config);
            stream.write(json.getBytes(StandardCharsets.UTF_8));
            stream.close();
        } catch (IOException e) {
            System.out.println("Error occurred while creating config file " + e.getMessage());
        }
        return config;
    }
}
