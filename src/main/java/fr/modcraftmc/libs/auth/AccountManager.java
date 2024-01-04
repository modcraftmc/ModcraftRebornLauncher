package fr.modcraftmc.libs.auth;

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
                if (ModcraftApplication.launcherConfig.isKeeplogin()) AsyncExecutor.runAsync(() -> AccountManager.saveLoginInfos(mcProfile));
                return new AuthResult(true, mcProfile);

            } catch (Exception e) {
                ErrorsHandler.handleError(e);
                return new AuthResult(false, null);
            }
        });
    }

    public static AuthResult validate(Label loadingMessage) {
           if (!ModcraftApplication.launcherConfig.isKeeplogin()) return new AuthResult(false, null);

           if (loadingMessage != null )
               Platform.runLater(() -> loadingMessage.setText("Vérification du compte..."));

           try {
               JsonObject json = getLoginJson();

               StepMCProfile.MCProfile jsonProfile = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.fromJson(json);
               try (final CloseableHttpClient httpClient = MicrosoftConstants.createHttpClient()) {
                   StepMCProfile.MCProfile refreshedProfile  = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.refresh(httpClient, jsonProfile);
                   AsyncExecutor.runAsync(() -> saveLoginInfos(refreshedProfile));
                   return new AuthResult(true, refreshedProfile);
               }

           } catch (Exception e) {
               //ErrorsHandler.handleError(e);
               return new AuthResult(false, null);
           }
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
