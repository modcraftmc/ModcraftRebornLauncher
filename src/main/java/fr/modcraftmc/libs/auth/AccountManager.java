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

public class AccountManager {

    private static AzAuthenticator aZauthenticator = new AzAuthenticator("https://modcraftmc.fr");
    private static Authenticator authenticator = new Authenticator(Authenticator.MOJANG_AUTH_URL, AuthPoints.NORMAL_AUTH_POINTS);

    private static AuthInfos authInfos;

    public static boolean tryLogin(String username, String password) {

        try {

            AuthResponse response = authenticator.authenticate(AuthAgent.MINECRAFT, username, password, UUID.randomUUID().toString());

            authInfos = new AuthInfos(response.getSelectedProfile().getName(), response.getAccessToken(), response.getSelectedProfile().getId());


            String tokenToSave = response.getAccessToken() + "!Tpy2B5-~9!" + response.getClientToken();
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
    }

    public static boolean tryVerify(String accessToken) {

        try {
            String crypto = new String(Base64.getDecoder().decode(accessToken));
            String[] split = crypto.split("!Tpy2B5-~9!");

            if (split.length == 1) {

                User refresh = aZauthenticator.verify(split[0]);
                authInfos = new AuthInfos(refresh.getUsername(), refresh.getAccessToken(), refresh.getUuid().toString());

                String tokenToSave = refresh.getAccessToken();
                String tosave = Base64.getEncoder().withoutPadding().encodeToString(tokenToSave.getBytes());
                ModcraftApplication.launcherConfig.setAccesToken(tosave);
                ModcraftApplication.launcherConfig.save();

            } else {

                RefreshResponse refresh = authenticator.refresh(split[0], split[1]);
                authInfos = new AuthInfos(refresh.getSelectedProfile().getName(), refresh.getAccessToken(), refresh.getSelectedProfile().getId());

                String tokenToSave = refresh.getAccessToken() + "!Tpy2B5-~9!" + refresh.getClientToken();
                String tosave = Base64.getEncoder().withoutPadding().encodeToString(tokenToSave.getBytes());
                ModcraftApplication.launcherConfig.setAccesToken(tosave);
                ModcraftApplication.launcherConfig.save();
            }



            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean logout(String accesToken) {


        return false;
    }

    public static AuthInfos getAuthInfos() {
        return authInfos;
    }
}
