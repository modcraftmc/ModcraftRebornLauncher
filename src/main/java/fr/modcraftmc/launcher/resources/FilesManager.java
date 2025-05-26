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

    private final char FP = File.separatorChar;
    public static Path BASE_PATH;
    public static Path DEFAULT_PATH;
    public static Path LAUNCHER_PATH;
    public static Path LAUNCHER_JAR;
    public static Path LOGS_PATH;
    public static Path OPTIONS_FILE;
    public static Path INSTANCES_PATH;
    public static Path JAVA_PATH;
    public static Path JAVA_EXE;

    public void init() {

        BASE_PATH = this.getBasePath();
        DEFAULT_PATH = BASE_PATH.resolve(".modcraftmc" + (ModcraftApplication.ENVIRONMENT.getEnv() == Environment.ENV.DEV ? "-dev" : ""));
        LAUNCHER_PATH = DEFAULT_PATH.resolve("launcher");
        LAUNCHER_JAR = LAUNCHER_PATH.resolve("launcher.jar");
        LOGS_PATH = LAUNCHER_PATH.resolve("logs");
        OPTIONS_FILE = LAUNCHER_PATH.resolve("modcraftlauncher.json");
        INSTANCES_PATH = DEFAULT_PATH.resolve("instances");
        JAVA_PATH = DEFAULT_PATH.resolve("java");
        JAVA_EXE = JAVA_PATH.resolve("bin").resolve("java");

        try {
            Files.createDirectories(BASE_PATH);
            Files.createDirectories(DEFAULT_PATH);
            Files.createDirectories(LAUNCHER_PATH);
            Files.createDirectories(LOGS_PATH);
            Files.createDirectories(INSTANCES_PATH);
            Files.createDirectories(JAVA_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create necessary directories: " + e.getMessage(), e);
        }

        setExecutablePermissions();
    }

    public Path getBasePath() {
        return switch (ModcraftApplication.ENVIRONMENT.getOS()) {
            case WINDOWS -> Path.of(System.getenv("appdata"));
            case LINUX -> {
                if (System.getenv("XDG_DATA_HOME") != null) {
                    yield Path.of(System.getenv("XDG_DATA_HOME"));
                }
                yield Path.of(System.getenv("HOME")).resolve(".local/share");
            }
            case MAC -> Path.of(System.getenv("HOME"));
            default -> throw new UnsupportedOperationException("Unsupported OS: " + ModcraftApplication.ENVIRONMENT.getOS());
        };
    }

    /**
     * From https://github.com/Arinonia/altiscore-bootstrap/commit/5eb60d0b458de16e308ae10ce7a970fb1885e94f
     */
    private void setExecutablePermissions() {
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
            final Path filePath = JAVA_PATH.resolve(execFile);
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
}
