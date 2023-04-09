package fr.modcraftmc.libs.auth;

//import com.azuriom.azauth.exception.AuthException;
//import com.azuriom.azauth.AuthClient;
//import com.azuriom.azauth.model.User;

import fr.modcraftmc.launcher.ModcraftApplication;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.login.User;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class AccountManager {
    private static Optional<User> authInfos = Optional.empty();

    public static CompletableFuture<Boolean> authenticate(boolean tryValidateAccessToken, BiConsumer<URL, Runnable> urlAndCanceler) {
        AtomicReference<URL> url = new AtomicReference<>();

        CompletableFuture completableFuture = CompletableFuture.supplyAsync(() -> {
            if (tryValidateAccessToken) {
                try {
                    if (!ModcraftApplication.launcherConfig.isKeeplogin()) return false;
                    String refreshTokenDecoded = new String(Base64.getDecoder().decode(ModcraftApplication.launcherConfig.getRefreshToken()));
                    Authenticator authenticator = new MicrosoftAuthentication().validate(refreshTokenDecoded);
                    authInfos = authenticator.getUser();
                } catch (AuthenticationException | IOException e) {
                    e.printStackTrace();
                }

                return authInfos.isPresent();
            }

            try {
                Authenticator authenticator = new MicrosoftAuthentication().runInitalAuthentication(url::set);
                authInfos = authenticator.getUser();
                String crypto = Base64.getEncoder().withoutPadding().encodeToString(authenticator.getResultFile().writeString().getBytes());
                ModcraftApplication.launcherConfig.setRefreshToken(crypto);
                ModcraftApplication.launcherConfig.save();
            } catch (AuthenticationException | IOException e) {
                e.printStackTrace();
            }
            return authInfos.isPresent();
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

        if (!tryValidateAccessToken)
            waitValue.start();
        else {
            try {
                urlAndCanceler.accept(new URL("https://dummy"), () -> completableFuture.complete(false));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return completableFuture;
    }

    public static CompletableFuture<Boolean> tryLogin(boolean validate, BiConsumer<URL, Runnable> urlAndCanceler) {
        AtomicReference<URL> url = new AtomicReference<>();
        CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {

                if (validate) {
                    if (!ModcraftApplication.launcherConfig.isKeeplogin()) return false;
                    String refreshTokenDecoded = new String(Base64.getDecoder().decode(ModcraftApplication.launcherConfig.getRefreshToken()));

                    Authenticator authenticator = new MicrosoftAuthentication().validate(refreshTokenDecoded);
                    Optional<User> user = authenticator.getUser();
                    authInfos = user;
                    url.set(new URL("https://localhost")); //TODO: rework this

                    String tosave = Base64.getEncoder().withoutPadding().encodeToString(authenticator.getResultFile().writeString().getBytes());
                    ModcraftApplication.launcherConfig.setRefreshToken(tosave);
                    ModcraftApplication.launcherConfig.save();
                    return user.isPresent();
                } else {
                    Authenticator authenticator = new MicrosoftAuthentication().runInitalAuthentication(url::set);
                    Optional<User> user = authenticator.getUser();
                    authInfos = user;
                    String crypto = Base64.getEncoder().withoutPadding().encodeToString(authenticator.getResultFile().writeString().getBytes());
                    ModcraftApplication.launcherConfig.setRefreshToken(crypto);
                    ModcraftApplication.launcherConfig.save();
                    return user.isPresent();
                }


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

    public static Optional<User> getAuthInfos() {
        return authInfos;
    }
}
