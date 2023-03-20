package fr.modcraftmc.libs.auth;

//import com.azuriom.azauth.exception.AuthException;
//import com.azuriom.azauth.AuthClient;
//import com.azuriom.azauth.model.User;

import fr.modcraftmc.launcher.ModcraftApplication;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.login.User;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AccountManager {

    //private static AuthClient aZauthenticator = new AuthClient("https://modcraftmc.fr");
   // public static final Authenticator authenticator = Authenticator.ofMicrosoft().customAzureApplication()

    private static Optional<User> authInfos;

    public static enum LoginType {
        MICROSOFT, MOJANG, MODCRAFT
    }

    public static CompletableFuture<Boolean> tryLogin(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Authenticator authenticator = new MicrosoftAuthentication().runInitalAuthentication();

                Optional<User> user = authenticator.getUser();

                System.out.println(user.isPresent());
                authInfos = user;
                return user.isPresent();

            } catch (IOException | AuthenticationException e) {
                e.printStackTrace();
            }
            return false;
        });
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//
//                AuthResponse response = authenticator.authenticate(AuthAgent.MINECRAFT, username, password, "mdcraft");
//                authInfos = new AuthInfos(response.getSelectedProfile().getName(), response.getAccessToken(), response.getSelectedProfile().getId());
//
//                String tokenToSave = response.getAccessToken();
//                String crypto = Base64.getEncoder().withoutPadding().encodeToString(tokenToSave.getBytes());
//
//                ModcraftApplication.launcherConfig.setAccesToken(crypto);
//                ModcraftApplication.launcherConfig.save();
//                return true;
//            } catch (fr.litarvan.openauth.AuthenticationException e) {
//                e.printStackTrace();
//            }
//            return false;
//        });
    }

    public static CompletableFuture<Boolean> tryVerify(String refreshToken) {
        if (!ModcraftApplication.launcherConfig.isKeeplogin()) return CompletableFuture.completedFuture(false);
        return CompletableFuture.supplyAsync(() -> {
//            try {
//                String refreshTokenDecoded = new String(Base64.getDecoder().decode(refreshToken));
//                MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
//                MicrosoftAuthResult result = authenticator.loginWithRefreshToken(refreshTokenDecoded);
//                String tosave = Base64.getEncoder().withoutPadding().encodeToString(result.getRefreshToken().getBytes());
//                ModcraftApplication.launcherConfig.setRefreshToken(tosave);
//                ModcraftApplication.launcherConfig.save();
//                authInfos = result.getProfile();
//                return true;
//            } catch (MicrosoftAuthenticationException e) {
//                e.printStackTrace();
//            }
            return false;
        });
//        if (!ModcraftApplication.launcherConfig.isKeeplogin()) return CompletableFuture.completedFuture(false);
//        return CompletableFuture.supplyAsync(() -> {
//            String crypto = new String(Base64.getDecoder().decode(accessToken));
//            try {
//                RefreshResponse refresh = authenticator.refresh(crypto, "mdcraft");
//                authInfos = new AuthInfos(refresh.getSelectedProfile().getName(), refresh.getAccessToken(), refresh.getSelectedProfile().getId());
//
//                String tokenToSave = refresh.getAccessToken();
//                String tosave = Base64.getEncoder().withoutPadding().encodeToString(tokenToSave.getBytes());
//                ModcraftApplication.launcherConfig.setAccesToken(tosave);
//                return true;
//            } catch (fr.litarvan.openauth.AuthenticationException authenticationException) {
//                authenticationException.printStackTrace();
//            }
//
//            return false;
//        });
    }

    public static Optional<User> getAuthInfos() {
        return authInfos;
    }
}
