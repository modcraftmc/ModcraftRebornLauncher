package fr.modcraftmc.libs.updater;

public class UpdateResult {

    private boolean IsSuccess;
    public UpdateResult(boolean IsSuccess) {
        this.IsSuccess = IsSuccess;
    }


    public static UpdateResult success() {
        return new UpdateResult(true);
    }

    public static UpdateResult faillure() {
        return new UpdateResult(false);
    }
}
