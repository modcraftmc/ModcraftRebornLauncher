package fr.modcraftmc.libs.auth;

//import com.azuriom.azauth.exception.AuthException;
//import com.azuriom.azauth.AuthClient;
//import com.azuriom.azauth.model.User;
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

    //private static AuthClient aZauthenticator = new AuthClient("https://modcraftmc.fr");
    private static Authenticator authenticator = new Authenticator(Authenticator.MOJANG_AUTH_URL, AuthPoints.NORMAL_AUTH_POINTS);

    private static AuthInfos authInfos;

    public static enum LoginType {
        MICROSOFT, MOJANG, MODCRAFT
    }

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
                e.printStackTrace();
            }
            return false;
        });
    }

    public static CompletableFuture<Boolean> tryVerify(String accessToken) {
        if (!ModcraftApplication.launcherConfig.isKeeplogin()) return CompletableFuture.completedFuture(false);
        return CompletableFuture.supplyAsync(() -> {
            String crypto = new String(Base64.getDecoder().decode(accessToken));
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
        });
    }

    public static AuthInfos getAuthInfos() {
        return authInfos;
    }
}
