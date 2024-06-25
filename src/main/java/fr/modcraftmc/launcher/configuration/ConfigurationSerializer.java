package fr.modcraftmc.launcher.configuration;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

public class ConfigurationSerializer {

    public static JsonSerializer<InstanceProperty> instancePropertyJsonSerializer = (src, typeOfSrc, context) -> {
        JsonObject json = new JsonObject();
        json.addProperty("useCustomPath", src.customInstance());
        json.addProperty("customPath", src.customInstancePath());
        return json;
    };

    public static JsonDeserializer<InstanceProperty> instancePropertyJsonDeserializer = (src, typeOfSrc, context) -> {
        JsonObject jobject = src.getAsJsonObject();

        return new InstanceProperty(jobject.get("useCustomPath").getAsBoolean(), jobject.get("customPath").getAsString());

    };
}
