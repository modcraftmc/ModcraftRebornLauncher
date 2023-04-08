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
import java.net.URL;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AccountManager {

    //private static AuthClient aZauthenticator = new AuthClient("https://modcraftmc.fr");
   // public static final Authenticator authenticator = Authenticator.ofMicrosoft().customAzureApplication()

    private static Optional<User> authInfos;

    public static enum LoginType {
        MICROSOFT, MOJANG, MODCRAFT
    }

    public static CompletableFuture<Boolean> tryLogin(BiConsumer<URL, Runnable> urlAndCanceler) {
        AtomicReference<URL> url = new AtomicReference<>();
        CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Authenticator authenticator = new MicrosoftAuthentication().runInitalAuthentication(url::set);
                Optional<User> user = authenticator.getUser();
                authInfos = user;
                String crypto = Base64.getEncoder().withoutPadding().encodeToString(authenticator.getResultFile().writeString().getBytes());
                ModcraftApplication.launcherConfig.setRefreshToken(crypto);
                ModcraftApplication.launcherConfig.save();
                return user.isPresent();

            } catch (IOException | AuthenticationException e) {
                e.printStackTrace();
            }
            return false;
        });

        Thread waitValue = new Thread(() -> {
           while (url.get() == null) {
               try {
                   Thread.sleep(10);
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }
           }
           urlAndCanceler.accept(url.get(), () -> completableFuture.complete(false));
        });

        waitValue.start();
        return completableFuture;
    }

    public static CompletableFuture<Boolean> tryVerify(String refreshToken) {
        if (!ModcraftApplication.launcherConfig.isKeeplogin()) return CompletableFuture.completedFuture(false);
        return CompletableFuture.supplyAsync(() -> {
            try {
                String refreshTokenDecoded = new String(Base64.getDecoder().decode(refreshToken));

                Authenticator authenticator = new MicrosoftAuthentication().validate(refreshTokenDecoded);
                Optional<User> user = authenticator.getUser();
                authInfos = user;

                String tosave = Base64.getEncoder().withoutPadding().encodeToString(authenticator.getResultFile().writeString().getBytes());
                ModcraftApplication.launcherConfig.setRefreshToken(tosave);
                ModcraftApplication.launcherConfig.save();
                return true;
            } catch (IOException | AuthenticationException e) {
                e.printStackTrace();
            }
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
