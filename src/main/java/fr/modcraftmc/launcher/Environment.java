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

    private ENV env;
    private OS os;

    public Environment(ENV environment, OS operatingSystem) {
        this.env = environment;
        this.os = operatingSystem;
    }

    public ENV getEnv() {
        return env;
    }

    public OS getOS() {
        return os;
    }

    @Override
    public String toString() {
        return "Environment: " + env + ", OS: " + os;
    }
}