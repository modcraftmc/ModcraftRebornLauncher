package fr.modcraftmc.libs.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.modcraftmc.launcher.Environment;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import okhttp3.*;

import java.io.IOException;

/**
 * WILL SOON BE MOVED IN ITS OWN PROJECT.
 */
public class ModcraftApiClient {
    public enum Privilege {
        USER,
        ADMIN,
        DICTATOR
    }

    public record ApiError(String error, String errorMessage) {}
    public record MaintenanceStatus(boolean isActivated, String reason) {}
    public record UserAccessToken(String token) {}
    public record UserAccessTokenResponse(boolean success, String message, UserAccessToken userAccessToken) {}
    public record UserInfo(String username, String uuid, Privilege privilege) {}
    public static final String UNKNOWN_API_ERROR_MESSAGE = "Unknown API error";
    public static final Exception MODCRAFT_API_CLIENT_NOT_INITIALIZED = new Exception("ModcraftApiClient is not initialized.");
    private static final String API_URL = ModcraftApplication.ENVIRONMENT == Environment.ENV.PROD ? "https://api.modcraftmc.fr/v1" : "http://192.168.0.103:3000/v1";
    private static final String LAUNCHER_MAINTENANCE_ROUTE = "/launcher/maintenance";
    private static final String USER_LOGIN_ROUTE = "/auth/login";
    private static final String USER_INFO_ROUTE = "/users/getUserInfo";
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
            ApiError apiError = tryGetError(json);
            if (apiError != null) {
                return new UserAccessTokenResponse(false, apiError.errorMessage(), null);
            }
            else {
                return new UserAccessTokenResponse(false, UNKNOWN_API_ERROR_MESSAGE, null);
            }
        }
        return new UserAccessTokenResponse(true, "", new UserAccessToken(token));
    }

    private static ApiError tryGetError(JsonObject json) {
        try {
            ApiError errorInfo = new ApiError(json.get("error").getAsString(), json.get("errorMessage").getAsString());
            ErrorsHandler.handleErrorWithCustomHeader(errorInfo.error(), new Exception(errorInfo.errorMessage()));
            return errorInfo;
        } catch (Exception e) {
            ErrorsHandler.handleErrorWithCustomHeader(UNKNOWN_API_ERROR_MESSAGE, new Exception("An unknown error occured while contacting modcraft servers : " + e.getMessage(), e));
            return null;
        }
    }

    public static UserInfo getUserInfo(UserAccessToken token){
        String response = null;
        try {
            response = getSecure(USER_INFO_ROUTE, token.token());
        } catch (IOException e) {
            raiseUnknownApiError(e);
            return null;
        }

        ModcraftApplication.LOGGER.info("UserInfo: " + response);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        String username = null;
        String uuid = null;
        Privilege privilege = null;
        try {
            username = json.get("name").getAsString();
            uuid = json.get("uuid").getAsString();
            privilege = Privilege.valueOf(json.get("privilege").getAsString().toUpperCase());
        } catch (Exception e) {
            ApiError apiError = tryGetError(json);
            return null;
        }

        return new UserInfo(username, uuid, privilege);
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

    static String getSecure(String route, String token) throws IOException {
        if(client == null) {
            ErrorsHandler.handleError(MODCRAFT_API_CLIENT_NOT_INITIALIZED);
            return null;
        }
        Request request = new Request.Builder()
                .url(API_URL + route)
                .addHeader("Authorization", "Bearer " + token)
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

    public static class ModcraftServiceUserProfile {
        public UserAccessToken token;
        public UserInfo info;

        public ModcraftServiceUserProfile(UserAccessToken token, UserInfo info) {
            this.token = token;
            this.info = info;
        }

        public static ModcraftServiceUserProfile getProfile(UserAccessToken token) {
            return new ModcraftServiceUserProfile(token, getUserInfo(token));
        }

        public static ModcraftServiceUserProfile getProfile(String mcAccessToken) {
            UserAccessTokenResponse tokenResponse = getUserAccessToken(mcAccessToken);
            if (!tokenResponse.success()) {
                return null;
            }

            return getProfile(tokenResponse.userAccessToken());
        }
    }
}
