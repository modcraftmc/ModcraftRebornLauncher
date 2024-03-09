package fr.modcraftmc.libs.api;

import fr.modcraftmc.api.ModcraftApiRequestsExecutor;
import fr.modcraftmc.api.models.UserInfo;
import fr.modcraftmc.launcher.ModcraftApplication;

public class ModcraftServiceUserProfile {
    public UserInfo info;

    public ModcraftServiceUserProfile(UserInfo info) {
        this.info = info;
    }

    public static ModcraftServiceUserProfile getProfile(String mcAccessToken) throws Exception {
        String token = ModcraftApplication.apiClient.executeRequest(ModcraftApiRequestsExecutor.login(mcAccessToken));
        ModcraftApplication.apiClient.setToken(token);
        return new ModcraftServiceUserProfile(ModcraftApplication.apiClient.executeRequest(ModcraftApiRequestsExecutor.getUserInfo()));
    }
}