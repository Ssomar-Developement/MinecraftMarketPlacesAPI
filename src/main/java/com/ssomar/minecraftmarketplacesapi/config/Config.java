package com.ssomar.minecraftmarketplacesapi.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private static Config instance;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }

        return instance;
    }

    private JsonObject root;

    private Config() {
        File file = new File("config.json");

        //File file = new File("C:\\Users\\marce\\Documents\\GitHub\\DiscordStripeBot\\config.json");

        if (!file.exists()) {
            try {
                InputStream src = Config.class.getResourceAsStream("/config.json");
                Files.copy(src, Paths.get(file.toURI()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            String json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            JsonParser jsonParser = new JsonParser();
            root = (JsonObject) jsonParser.parse(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getToken() {
        return root.get("token").getAsString();
    }

    public String getDiscordServerID() {
        return root.get("discordServerID").getAsString();
    }

    public String getMessageWait() {
        return root.get("messageWait").getAsString();
    }

    public String getMessageError() {
        return root.get("messageError").getAsString();
    }

    public String getMessageValid() {
        return root.get("messageValid").getAsString();
    }

    public String getUserToPingIfProblem() {
        return root.get("userToPingIfProblem").getAsString();
    }

    public List<String> getStripeAPIKeys() {
        List<String> keys = new ArrayList<>();

        JsonObject stripeAPIKeys = root.get("stripeAPIKeys").getAsJsonObject();

        for (String id : stripeAPIKeys.keySet()) {
            String key = stripeAPIKeys.get(id).getAsString();
            if (key.contains("...")) continue;
            keys.add(key);
        }
        return keys;
    }

    public String getSpigotUsername() {
        return root.get("spigotUsername").getAsString();
    }

    public String getSpigotPassword() {
        return root.get("spigotPassword").getAsString();
    }

    public String getBuiltByBitUsername() {
        return root.get("builtByBitUsername").getAsString();
    }

    public String getBuiltByBitPassword() {
        return root.get("builtByBitPassword").getAsString();
    }

    public String getPolymartUsername() {
        return root.get("polymartUsername").getAsString();
    }

    public String getPolymartPassword() {
        return root.get("polymartPassword").getAsString();
    }

    public String getUserDataPath() {
        return root.get("userDataPath").getAsString();
    }
}
