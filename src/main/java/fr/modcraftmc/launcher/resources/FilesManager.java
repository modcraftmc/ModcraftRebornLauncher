package fr.modcraftmc.launcher.resources;

import fr.modcraftmc.launcher.Environment;
import fr.modcraftmc.launcher.ModcraftApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public class FilesManager {

    public static char FP = File.separatorChar;
    public static String BASE_PATH;
    public static File DEFAULT_PATH;
    public static File LAUNCHER_PATH;
    public static File LAUNCHER_JAR;
    public static File LOGS_PATH;
    public static File OPTIONS_PATH;
    public static File INSTANCES_PATH;
    public static File JAVA_PATH;
    public static File JAVA_EXE;

    public void init() {

        BASE_PATH = ModcraftApplication.ENVIRONMENT.getOS() == Environment.OS.WINDOWS ? System.getenv("appdata") : System.getenv("HOME");
        DEFAULT_PATH = new File(BASE_PATH + FP + ".modcraftmc" + (ModcraftApplication.ENVIRONMENT.getEnv() == Environment.ENV.DEV ? "-dev" : "") + FP);
        LAUNCHER_PATH = new File(DEFAULT_PATH, "launcher");
        LAUNCHER_JAR = new File(LAUNCHER_PATH, "launcher.jar");
        LOGS_PATH = new File(LAUNCHER_PATH, "logs");
        OPTIONS_PATH = new File(LAUNCHER_PATH, "modcraftlauncher.json");
        INSTANCES_PATH = new File(DEFAULT_PATH, "instances");
        JAVA_PATH = new File(DEFAULT_PATH, "java");
        JAVA_EXE = new File(JAVA_PATH, "bin/java");

        try {
            if (!DEFAULT_PATH.exists()) {
                DEFAULT_PATH.mkdirs();
            }
            if (!LAUNCHER_PATH.exists()) {
                LAUNCHER_PATH.mkdirs();
            }
            if (!OPTIONS_PATH.exists()) {
                OPTIONS_PATH.createNewFile();
            }
            if (!INSTANCES_PATH.exists()) {
                INSTANCES_PATH.mkdirs();
            }
            if (!JAVA_PATH.exists()) {
                JAVA_PATH.mkdirs();
            }

            if (!LOGS_PATH.exists()) {
                LOGS_PATH.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setExecutablePermissions();
    }


    /**
     * From https://github.com/Arinonia/altiscore-bootstrap/commit/5eb60d0b458de16e308ae10ce7a970fb1885e94f
     */
    private static void setExecutablePermissions() {
        if (ModcraftApplication.ENVIRONMENT.getOS() != Environment.OS.LINUX) {
            return;
        }
        System.out.println("Setting executable permissions for runtime binaries...");
        final String[] executableFiles = {
                "bin/java",
                "bin/keytool",
                "bin/jpackage",
                "lib/jspawnhelper"
        };

        final Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.GROUP_EXECUTE);
        permissions.add(PosixFilePermission.OTHERS_READ);
        permissions.add(PosixFilePermission.OTHERS_EXECUTE);

        for (final String execFile : executableFiles) {
            final Path filePath = FilesManager.JAVA_PATH.toPath().resolve(execFile);
            if (Files.exists(filePath)) {
                try {
                    Files.setPosixFilePermissions(filePath, permissions);
                    System.out.println("Set executable permissions for: " + filePath);
                } catch (final IOException e) {
                    System.err.println("Warning: Could not set permissions for " + filePath + ": " + e.getMessage());
                }
            }
        }

    }

    public File getDefaultPath() {
        return DEFAULT_PATH;
    }

    public void setDefaultPath(File defaultPath) {
        DEFAULT_PATH = defaultPath;
    }

    public File getOptionsPath() {
        return OPTIONS_PATH;
    }

    public void setOptionsPath(File optionsPath) {
        OPTIONS_PATH = optionsPath;
    }

    public File getInstancesPath() {
        return INSTANCES_PATH;
    }

    public void setInstancesPath(File instancesPath) {
        INSTANCES_PATH = instancesPath;
    }

    public File getJavaPath() {
        return JAVA_PATH;
    }

    public static void setJavaPath(File javaPath) {
        JAVA_PATH = javaPath;
    }

    public File getLauncherPath() {
        return LAUNCHER_PATH;
    }

    public void setLauncherPath(File launcherPath) {
        LAUNCHER_PATH = launcherPath;
    }
}
