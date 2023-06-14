package fr.modcraftmc.libs.auth;

//import com.azuriom.azauth.exception.AuthException;
//import com.azuriom.azauth.AuthClient;
//import com.azuriom.azauth.model.User;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.modcraftmc.launcher.AsyncExecutor;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import javafx.application.Platform;
import javafx.scene.control.Label;
import net.raphimc.mcauth.MinecraftAuth;
import net.raphimc.mcauth.step.java.StepMCProfile;
import net.raphimc.mcauth.step.msa.StepMsaDeviceCode;
import net.raphimc.mcauth.util.MicrosoftConstants;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AccountManager {
   public static class AuthResult {
       private final boolean isLoggedIn;
       private final StepMCProfile.MCProfile mcProfile;

       public AuthResult(boolean isLoggedIn, @Nullable StepMCProfile.MCProfile mcProfile) {
           this.isLoggedIn = isLoggedIn;
           this.mcProfile = mcProfile;
       }

       public boolean isLoggedIn() {
           return isLoggedIn;
       }

       public StepMCProfile.MCProfile getMcProfile() {
           return mcProfile;
       }
   }

    public static CompletableFuture<AuthResult> authenticate(Consumer<StepMsaDeviceCode.MsaDeviceCode> callback) {
        return CompletableFuture.supplyAsync(() -> {

            StepMCProfile.MCProfile mcProfile;
            try (final CloseableHttpClient httpClient = MicrosoftConstants.createHttpClient()) {
                mcProfile = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(httpClient, new StepMsaDeviceCode.MsaDeviceCodeCallback(callback));
                System.out.println("Logged in as: " + mcProfile.name());
                if (ModcraftApplication.launcherConfig.isKeeplogin()) AsyncExecutor.runAsync(() -> AccountManager.saveLoginInfos(mcProfile));
                return new AuthResult(true, mcProfile);

            } catch (Exception e) {
                ErrorsHandler.handleError(e);
                return new AuthResult(false, null);
            }
        });
    }

    public static CompletableFuture<AuthResult> validate(Label loadingMessage) {
       return CompletableFuture.supplyAsync(() -> {
           if (!ModcraftApplication.launcherConfig.isKeeplogin()) return new AuthResult(false, null);

           Platform.runLater(() -> loadingMessage.setText("Vérification du compte..."));

           StepMCProfile.MCProfile loadedProfile;
           try {
               JsonObject json = getLoginJson();

               loadedProfile = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.fromJson(json);
               try (final CloseableHttpClient httpClient = MicrosoftConstants.createHttpClient()) {
                   loadedProfile  = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.refresh(httpClient, loadedProfile);
                   StepMCProfile.MCProfile finalLoadedProfile = loadedProfile;
                   AsyncExecutor.runAsync(() -> saveLoginInfos(finalLoadedProfile));
                   return new AuthResult(true, loadedProfile);
               }

           } catch (Exception e) {
               ErrorsHandler.handleError(e);
               return new AuthResult(false, null);
           }

       });
    }

    private static void saveLoginInfos(StepMCProfile.MCProfile profile) {
       ModcraftApplication.launcherConfig.setRefreshToken(profile.toJson().toString());
       ModcraftApplication.launcherConfig.save();
    }

    private static JsonObject getLoginJson() {
       String authJson = ModcraftApplication.launcherConfig.getRefreshToken();
       return JsonParser.parseString(authJson).getAsJsonObject();
    }
}
