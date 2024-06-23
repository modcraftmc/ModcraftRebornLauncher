package fr.modcraftmc.libs.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.modcraftmc.launcher.AsyncExecutor;
import fr.modcraftmc.launcher.MFXMLLoader;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import fr.modcraftmc.libs.popup.PopupBuilder;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.java.StepMCProfile;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;
import net.raphimc.minecraftauth.util.MicrosoftConstants;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class AccountManager {

    private StepMCProfile.MCProfile currentMCProfile;

    private static final StepFullJavaSession deviceCodeAuthStep = MinecraftAuth.builder()
            .withTimeout(300)
            .withClientId(MicrosoftConstants.JAVA_TITLE_ID)
            .withScope(MicrosoftConstants.SCOPE_TITLE_AUTH)
            .deviceCode()
            .withoutDeviceToken()
            .regularAuthentication(MicrosoftConstants.JAVA_XSTS_RELYING_PARTY)
            .buildMinecraftJavaProfileStep(false); // for chat signing stuff which we don't implement (yet)

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

    public void setCurrentMCProfile(StepMCProfile.MCProfile currentMCProfile) {
        this.currentMCProfile = currentMCProfile;
    }

    public StepMCProfile.MCProfile getCurrentMCProfile() {
        if (currentMCProfile == null)
            ErrorsHandler.handleError(new Exception());
        return this.currentMCProfile;
    }

    public static CompletableFuture<AuthResult> authenticate(Consumer<StepMsaDeviceCode.MsaDeviceCode> callback) {
        return CompletableFuture.supplyAsync(() -> {

            MinecraftAuth.USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
            try  {
                HttpClient httpClient = MinecraftAuth.createHttpClient();
                StepFullJavaSession.FullJavaSession javaSession = deviceCodeAuthStep.getFromInput(httpClient, new StepMsaDeviceCode.MsaDeviceCodeCallback(callback));
                if (ModcraftApplication.launcherConfig.isKeeplogin()) AsyncExecutor.runAsync(() -> AccountManager.saveLoginInfos(javaSession));
                return new AuthResult(true, javaSession.getMcProfile());

            } catch (Exception e) {
                if (e instanceof TimeoutException) {
                    Platform.runLater(() -> {
                        Alert popup = new PopupBuilder()
                                        .setHeader("Une erreur est survenue")
                                        .setText("Le délai de connexion a été dépassé.")
                                        .build();
                        popup.show();
                        ErrorsHandler.logException(e);
                        Scene scene = MFXMLLoader.loadFxml("login.fxml", true);
                        ModcraftApplication.switchScene(-1, -1, scene);
                    });
                    return new AuthResult(false, null);
                }

                Platform.runLater(() -> {
                    ErrorsHandler.handleError(e);
                    Scene scene = MFXMLLoader.loadFxml("login.fxml", true);
                    ModcraftApplication.switchScene(-1, -1, scene);
                });
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

               HttpClient httpClient = MinecraftAuth.createHttpClient();
               StepFullJavaSession.FullJavaSession jsonProfile = deviceCodeAuthStep.fromJson(json);
               StepFullJavaSession.FullJavaSession javaSession = deviceCodeAuthStep.refresh(httpClient, jsonProfile);

               AsyncExecutor.runAsync(() -> saveLoginInfos(javaSession));
               ModcraftApplication.accountManager.setCurrentMCProfile(javaSession.getMcProfile());
               return new AuthResult(true, javaSession.getMcProfile());

           } catch (Exception e) {
               //ErrorsHandler.handleError(e);
               return new AuthResult(false, null);
           }
    }

    private static void saveLoginInfos(StepFullJavaSession.FullJavaSession profile) {
       ModcraftApplication.launcherConfig.setRefreshToken(deviceCodeAuthStep.toJson(profile).toString());
       ModcraftApplication.launcherConfig.save();
    }

    private static JsonObject getLoginJson() {
       String authJson = ModcraftApplication.launcherConfig.getRefreshToken();
       return JsonParser.parseString(authJson).getAsJsonObject();
    }
}
