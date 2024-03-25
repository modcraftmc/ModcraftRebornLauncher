package fr.modcraftmc.libs.popup;

import javafx.scene.control.Alert;

public class PopupBuilder {

    private String header;
    private String text;

    public PopupBuilder setHeader(String header) {
        this.header = header;
        return this;
    }

    public PopupBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public Alert build() {
        Alert alert = new Alert(Alert.AlertType.ERROR, this.text);
        alert.setTitle("ModcraftMC");
        alert.setHeaderText(header);
        return alert;
    }
}
