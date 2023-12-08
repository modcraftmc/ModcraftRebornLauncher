package fr.modcraftmc.libs.httpRequests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import okhttp3.*;

import java.io.IOException;

public class ModcraftApiClient {

    public record MaintenanceStatus(boolean isActivated, String reason) {}
    public record UserAccessToken(String token) {}
    public record UserAccessTokenResponse(boolean success, String message, UserAccessToken userAccessToken) {}
    public static final String UNKNOWN_API_ERROR_MESSAGE = "Unknown API error";
    public static final Exception MODCRAFT_API_CLIENT_NOT_INITIALIZED = new Exception("ModcraftApiClient is not initialized.");
    private static final String API_URL = ModcraftApplication.ENVIRONMENT == ModcraftApplication.Environment.PROD ? "https://api.modcraftmc.fr/v1" : "http://localhost:3000/v1";
    private static final String LAUNCHER_MAINTENANCE_ROUTE = "/launcher/maintenance";
    private static final String USER_LOGIN_ROUTE = "/auth/login";
    private static final MediaType JSON = MediaType.get("application/json");
    private static OkHttpClient client;
    private static Gson gson = new Gson();

    public static void init() {
        client = new OkHttpClient();
    }

    public static void raiseUnknownApiError(Exception e) {
        ErrorsHandler.handleError(new Exception("An unknown error occured while contacting modcraft servers : " + e.getMessage(), e));
    }

    public static MaintenanceStatus areServersUnderMaintenance() {
        String response = null;
        try {
            response = get(LAUNCHER_MAINTENANCE_ROUTE);
        } catch (IOException e) {
            raiseUnknownApiError(e);
            return new MaintenanceStatus(true, UNKNOWN_API_ERROR_MESSAGE);
        }

        ModcraftApplication.LOGGER.info("Maintenance status: " + response);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return new MaintenanceStatus(json.get("maintenance").getAsBoolean(), json.get("reason").getAsString());
    }

    public static UserAccessTokenResponse getUserAccessToken(String mcAccessToken) {
        String response = null;
        JsonObject body = new JsonObject();
        body.addProperty("mcAccessToken", mcAccessToken);
        try {
            response = post(USER_LOGIN_ROUTE, gson.toJson(body));
        } catch (IOException e) {
            raiseUnknownApiError(e);
            return new UserAccessTokenResponse(false, UNKNOWN_API_ERROR_MESSAGE, null);
        }

        ModcraftApplication.LOGGER.info("UserAccessTokenResponse: " + response);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        String token = null;
        try {
             token = json.get("token").getAsString();
        }
        catch (Exception e) {
            try {
                ErrorsHandler.handleErrorWithCustomHeader(json.get("error").getAsString(), new Exception(json.get("errorMessage").getAsString()));
                return new UserAccessTokenResponse(false, json.get("errorMessage").getAsString(), null);
            } catch (Exception exception) {
                raiseUnknownApiError(e);
                return new UserAccessTokenResponse(false, UNKNOWN_API_ERROR_MESSAGE, null);
            }
        }
        return new UserAccessTokenResponse(true, "", new UserAccessToken(token));
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
