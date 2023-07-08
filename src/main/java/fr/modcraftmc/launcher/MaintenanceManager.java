package fr.modcraftmc.launcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class MaintenanceManager {
    private static final Gson gson = new GsonBuilder().create();

    public static MaintenanceStatus getMaintenanceStatusSync() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Utils.catchForbidden(new URL("https://download.modcraftmc.fr/maintenance.json"))));
            return gson.fromJson(reader, MaintenanceStatus.class);
        } catch (IOException e) {
            return new MaintenanceStatus(true, "Can't contact modcraft servers.");
        }
    }

    public record MaintenanceStatus(boolean isActivated, String reason) {}

}
