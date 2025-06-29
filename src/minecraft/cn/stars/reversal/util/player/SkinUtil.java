package cn.stars.reversal.util.player;

import cn.stars.reversal.GameInstance;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SkinUtil implements GameInstance {

    private static final Map<String, ResourceLocation> SKIN_CACHE = new HashMap<>();
    private static final Map<String, String> UUID_CACHE = new HashMap<>();
    private static final String NAME_TO_UUID = "https://api.mojang.com/users/profiles/minecraft/";

    public static ResourceLocation getResourceLocation(String uuid) {
        if (SKIN_CACHE.containsKey(uuid)) return SKIN_CACHE.get(uuid);

        String imageUrl = "https://crafatar.com/avatars/" + uuid;
        ResourceLocation resourceLocation = new ResourceLocation("skins/" + uuid + "?overlay=true");
        ThreadDownloadImageData headTexture = new ThreadDownloadImageData(null, imageUrl, null, null);
        mc.getTextureManager().loadTexture(resourceLocation, headTexture);
        SKIN_CACHE.put(uuid, resourceLocation);
        AbstractClientPlayer.getDownloadImageSkin(resourceLocation, uuid);
        return resourceLocation;
    }

    public static String uuidOf(String name) {
        try {
            if (UUID_CACHE.containsKey(name)) return UUID_CACHE.get(name);

            String data = scrape(NAME_TO_UUID + name);
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(data).getAsJsonObject();

            if (jsonObject == null || !jsonObject.has("id")) return "";

            UUID_CACHE.put(name, jsonObject.get("id").getAsString());
            return jsonObject.get("id").getAsString();
        } catch (IllegalStateException e) {
            return "";
        }
    }

    private static String scrape(String url) {
        StringBuilder content = new StringBuilder();
        try {
            final HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Chrome Version 88.0.4324.150");
            connection.connect();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            bufferedReader.close();
        } catch (IOException | ClassCastException ignored) {
        }
        return content.toString();
    }
}
