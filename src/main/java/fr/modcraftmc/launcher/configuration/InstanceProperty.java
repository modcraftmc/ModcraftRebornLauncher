package fr.modcraftmc.launcher.configuration;

public class InstanceProperty {

    private boolean customInstance;
    private String customInstancePath;

    public InstanceProperty(boolean customInstance, String customInstancePath) {
        this.customInstance = customInstance;
        this.customInstancePath = customInstancePath;
    }

    public boolean isCustomInstance() {
        return customInstance;
    }

    public String getCustomInstancePath() {
        return customInstancePath;
    }
}
