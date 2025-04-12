package fr.modcraftmc.launcher;

public class Environment {

    public enum ENV {
        DEV,
        PROD
    }

    public enum OS {
        WINDOWS,
        LINUX,
        MAC,
        OTHERS
    }

    private final ENV env;
    private final OS os;
    private final String apiUrl;

    public Environment(ENV environment, OS operatingSystem, String apiUrl) {
        this.env = environment;
        this.os = operatingSystem;
        this.apiUrl = apiUrl;
    }

    public ENV getEnv() {
        return env;
    }

    public OS getOS() {
        return os;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    @Override
    public String toString() {
        return "Environment: " + env + ", OS: " + os + ", API URL: " + apiUrl;
    }
}