// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package coffee.axle.proxy;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public class Config {
    private static final String CONFIG_PATH = Minecraft.getInstance().gameDirectory
            + "/config/CoffeeProxy.json";
    public static LinkedHashMap<String, String> proxies = new LinkedHashMap<>();
    public static String activeName = "";
    public static boolean showMultiplayerButton = true;

    public static void loadConfig() {
        File configFile = new File(CONFIG_PATH);
        try {
            if (!configFile.exists()) {
                // assume that if the config file doenst exist its prob first launch/whatever so just ... recreate it... you little weasle 
                FileUtils.touch(configFile);
                return;
            }
            String configString = FileUtils.readFileToString(configFile, "UTF-8");
            if (!configString.isEmpty()) {
                JsonObject configJson = JsonParser.parseString(configString).getAsJsonObject();
                JsonElement showBtn = configJson.get("show-multiplayer-button");
                Config.showMultiplayerButton = showBtn == null || showBtn.getAsBoolean();
                JsonElement active = configJson.get("active");
                Config.activeName = active == null ? "" : active.getAsString();
                proxies = new LinkedHashMap<>();
                JsonElement entries = configJson.get("proxies");
                if (entries != null) {
                    for (var entry : entries.getAsJsonObject().entrySet()) {
                        proxies.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading CoffeeProxy.json file");
            e.printStackTrace();
        }
    }

    public static void setActive(String name) {
        activeName = name == null ? "" : name;
        String entry = proxies.get(activeName);
        if (entry == null) {
            Coffeeproxy.proxy = new Proxy();
            Coffeeproxy.proxyEnabled = false;
        } else {
            Coffeeproxy.proxy = Proxy.parse(entry);
            Coffeeproxy.proxyEnabled = true;
        }
    }

    public static void saveConfig() {
        try {
            JsonObject configJson = new JsonObject();
            configJson.addProperty("show-multiplayer-button", showMultiplayerButton);
            configJson.addProperty("active", activeName);
            Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            configJson.add("proxies", gsonPretty.toJsonTree(proxies));
            FileUtils.write(new File(CONFIG_PATH), gsonPretty.toJson(configJson), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Error writing CoffeeProxy.json file");
            e.printStackTrace();
        }
    }
}
