package me.sqqo.overlay.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.sqqo.Pepita;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

import static me.sqqo.Pepita.mc;

public class Config {
    private int requestsDelay = 20;
    private int toggleKey = Keyboard.KEY_B;

    public void updateConfig(int requestsDelay, int toggleKey) {
        this.requestsDelay = requestsDelay;
        this.toggleKey = toggleKey;

        Pepita.getGuiOverlay().loadConfig(this);
    }

    private JsonObject getJson() {
        JsonObject object = new JsonObject();

        object.addProperty("requests_delay", requestsDelay);
        object.addProperty("toggle_key", toggleKey);

        return object;
    }

    public void saveConfig() {
        final File file = new File(mc.mcDataDir.getPath(), "Pepita");

        if (!file.exists()) {
            boolean success = file.mkdir();

            if (!success) {
                throw new RuntimeException("Failed to create Pepita directory...");
            }
        }

        final File cfg = new File(file, "config.json");

        final JsonObject object = getJson();

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String json = gson.toJson(object);

        try (FileWriter writer = new FileWriter(cfg)) {
            writer.write(json);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setupConfig() {
        final File file = new File(mc.mcDataDir.getPath(), "Pepita");

        if (!file.exists()) {
            file.mkdirs();
        }

        final File cfg = new File(file, "config.json");

        if (cfg.exists()) {
            return;
        }

        final JsonObject object = getJson();

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String json = gson.toJson(object);

        try (FileWriter writer = new FileWriter(cfg)) {
            writer.write(json);
        } catch (IOException ignored) {
        }
    }

    public void loadConfig() {
        File file = new File(mc.mcDataDir.getPath() + "/Pepita/", "config.json");

        if (!file.exists()) {
            return;
        }

        String result = "";

        try {
            result = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (result.isEmpty()) {
            return;
        }

        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(result).getAsJsonObject();

        if (object.has("requests_delay")) {
            this.requestsDelay = object.get("requests_delay").getAsInt();
        }

        if (object.has("toggle_key")) {
            this.toggleKey = object.get("toggle_key").getAsInt();
        }

        Pepita.getGuiOverlay().loadConfig(this);
    }
    public int getRequestsDelay() {
        return requestsDelay;
    }

    public int getToggleKey() {
        return toggleKey;
    }
}
