package fr.modcraftmc.libs.errors;

public class ErrorsHandler {

    public static void handleError(Exception exception) {
        //TODO: display error in a window
        exception.printStackTrace();
    }
}
