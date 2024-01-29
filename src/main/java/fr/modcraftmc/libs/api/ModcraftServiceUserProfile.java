package fr.modcraftmc.libs.api;

import fr.modcraftmc.api.ModcraftApiRequestsExecutor;
import fr.modcraftmc.api.models.UserInfo;
import fr.modcraftmc.launcher.ModcraftApplication;

public class ModcraftServiceUserProfile {
    public String token;
    public UserInfo info;

    public ModcraftServiceUserProfile(String token, UserInfo info) {
        this.token = token;
        this.info = info;
    }

    public static ModcraftServiceUserProfile getProfileWithModcraftToken(String token) throws Exception {
        return new ModcraftServiceUserProfile(token, ModcraftApplication.apiClient.executeRequest(ModcraftApiRequestsExecutor.getUserInfo(token)));
    }

    public static ModcraftServiceUserProfile getProfile(String mcAccessToken) throws Exception {
        return getProfileWithModcraftToken(ModcraftApplication.apiClient.executeRequest(ModcraftApiRequestsExecutor.login(mcAccessToken)));
    }
}