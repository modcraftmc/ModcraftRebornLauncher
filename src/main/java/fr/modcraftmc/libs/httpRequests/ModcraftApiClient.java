package fr.modcraftmc.libs.httpRequests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import fr.theshark34.openlauncherlib.util.CrashReporter;
import okhttp3.*;

import java.io.IOException;

public class ModcraftApiClient {
    public record MaintenanceStatus(boolean isActivated, String reason) {}
    public static final Exception MODCRAFT_API_CLIENT_NOT_INITIALIZED = new Exception("ModcraftApiClient is not initialized.");
    private static final String API_URL = ModcraftApplication.ENVIRONMENT == ModcraftApplication.Environment.PROD ? "https://api.modcraftmc.fr/v1" : "http://localhost:3000/v1";
    private static final String LAUNCHER_MAINTENANCE_ROUTE = "/launcher/maintenance";
    private static final MediaType JSON = MediaType.get("application/json");
    private static OkHttpClient client;
    private static Gson gson = new Gson();

    public static void init() {
        client = new OkHttpClient();
    }

    public static MaintenanceStatus areServersUnderMaintenance() {
        String response = null;
        try {
            response = get(LAUNCHER_MAINTENANCE_ROUTE);
        } catch (IOException e) {
            ErrorsHandler.handleErrorWithCustomHeader("Can't contact modcraft servers.", e);
            return new MaintenanceStatus(true, "Can't contact modcraft servers.");
        }

        ModcraftApplication.LOGGER.info("Maintenance status: " + response);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return new MaintenanceStatus(json.get("maintenance").getAsBoolean(), json.get("reason").getAsString());
    }

    static String get(String route) throws IOException {
        if(client == null) {
            ErrorsHandler.handleError(MODCRAFT_API_CLIENT_NOT_INITIALIZED);
            return null;
        }
        Request request = new Request.Builder()
                .url(API_URL + route)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    static String post(String url, String json) throws IOException {
        if(client == null) {
            ErrorsHandler.handleError(MODCRAFT_API_CLIENT_NOT_INITIALIZED);
            return null;
        }
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(API_URL + url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
