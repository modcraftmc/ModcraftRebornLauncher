package fr.modcraftmc.libs.auth;

import com.azuriom.azauth.AuthenticationException;
import com.azuriom.azauth.AzAuthenticator;
import com.azuriom.azauth.model.User;
import fr.litarvan.openauth.AuthPoints;
import fr.litarvan.openauth.Authenticator;
import fr.litarvan.openauth.model.AuthAgent;
import fr.litarvan.openauth.model.response.AuthResponse;
import fr.litarvan.openauth.model.response.RefreshResponse;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AccountManager {

    private static AzAuthenticator aZauthenticator = new AzAuthenticator("https://modcraftmc.fr");
    private static Authenticator authenticator = new Authenticator(Authenticator.MOJANG_AUTH_URL, AuthPoints.NORMAL_AUTH_POINTS);

    private static AuthInfos authInfos;

    public static CompletableFuture<Boolean> tryLogin(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                AuthResponse response = authenticator.authenticate(AuthAgent.MINECRAFT, username, password, "mdcraft");
                authInfos = new AuthInfos(response.getSelectedProfile().getName(), response.getAccessToken(), response.getSelectedProfile().getId());

                String tokenToSave = response.getAccessToken();
                String crypto = Base64.getEncoder().withoutPadding().encodeToString(tokenToSave.getBytes());

                ModcraftApplication.launcherConfig.setAccesToken(crypto);
                ModcraftApplication.launcherConfig.save();
                return true;
            } catch (fr.litarvan.openauth.AuthenticationException e) {

                try {
                    User authResponse = aZauthenticator.authenticate(username, password);

                    authInfos = new AuthInfos(authResponse.getUsername(), authResponse.getAccessToken(), authResponse.getUuid().toString());

                    String tokenToSave = authResponse.getAccessToken();
                    String crypto = Base64.getEncoder().withoutPadding().encodeToString(tokenToSave.getBytes());

                    ModcraftApplication.launcherConfig.setAccesToken(crypto);
                    ModcraftApplication.launcherConfig.save();
                    return true;

                } catch (IOException | AuthenticationException ioException) {
                    ioException.printStackTrace();
                }
            }
            return false;
        });
    }

    public static CompletableFuture<Boolean> tryVerify(String accessToken) {
        return CompletableFuture.supplyAsync(() -> {
            String crypto = new String(Base64.getDecoder().decode(accessToken));
            try {
                User refresh = aZauthenticator.verify(crypto);
                authInfos = new AuthInfos(refresh.getUsername(), refresh.getAccessToken(), refresh.getUuid().toString());

                String tokenToSave = refresh.getAccessToken();
                String tosave = Base64.getEncoder().withoutPadding().encodeToString(tokenToSave.getBytes());
                ModcraftApplication.launcherConfig.setAccesToken(tosave);


                return true;
            } catch (Exception e) {

                try {
                    RefreshResponse refresh = authenticator.refresh(crypto, "mdcraft");
                    authInfos = new AuthInfos(refresh.getSelectedProfile().getName(), refresh.getAccessToken(), refresh.getSelectedProfile().getId());

                    String tokenToSave = refresh.getAccessToken();
                    String tosave = Base64.getEncoder().withoutPadding().encodeToString(tokenToSave.getBytes());
                    ModcraftApplication.launcherConfig.setAccesToken(tosave);
                    return true;
                } catch (fr.litarvan.openauth.AuthenticationException authenticationException) {
                    authenticationException.printStackTrace();
                }

                return false;
            }
        });
    }

    public static AuthInfos getAuthInfos() {
        return authInfos;
    }
}
