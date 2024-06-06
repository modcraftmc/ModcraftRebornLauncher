package fr.modcraftmc.launcher.controllers.settings;

import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.launcher.controllers.BaseController;
import fr.modcraftmc.launcher.resources.FilesManager;
import io.github.palexdev.materialfx.controls.MFXSlider;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class GameController extends BaseController {

    @FXML private MFXSlider ramSlider;
    @FXML private Label ramText;

    @FXML private Button browseFile;
    @FXML private TextField customPath;
    @FXML private CheckBox customPathCheckbox;
    @FXML private CheckBox keepLauncherOpen;

    @Override
    public void initialize(FXMLLoader loader) {

        ramSlider.setMin(4);
        ramSlider.setMax(16);
        ramSlider.setValue(ModcraftApplication.launcherConfig.getRam());
        ramText.setText(String.format("%s Gb", (int) ramSlider.getValue()));

        customPathCheckbox.setSelected(ModcraftApplication.launcherConfig.getInstanceProperty().isCustomInstance());
        browseFile.setDisable(!customPathCheckbox.isSelected());
        customPathCheckbox.setText(ModcraftApplication.launcherConfig.getInstanceProperty().getCustomInstancePath());
        customPath.setText(ModcraftApplication.launcherConfig.getInstanceProperty().getCustomInstancePath());

        browseFile.setOnMouseClicked(event -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select directory");
            fileChooser.setInitialDirectory(FilesManager.INSTANCES_PATH);
            File path = fileChooser.showDialog(ModcraftApplication.getWindow());
            if (path != null) {
                customPath.setText(path.getAbsolutePath());
                ModcraftApplication.launcherConfig.setInstanceProperty(new InstanceProperty(true, customPath.getText()));
            }
        });

        customPathCheckbox.setOnMouseClicked(event -> {
            if (!customPathCheckbox.isSelected()) {
                ModcraftApplication.launcherConfig.setInstanceProperty(new InstanceProperty(false, ""));
            }
            browseFile.setDisable(!customPathCheckbox.isSelected());
            customPath.setDisable(!customPathCheckbox.isSelected());
        });

        customPath.setOnMouseClicked(event -> {
            if (!customPathCheckbox.isSelected()) {
                ModcraftApplication.launcherConfig.setInstanceProperty(new InstanceProperty(false, ""));
            }
            customPathCheckbox.setDisable(!customPathCheckbox.isSelected());
        });

        ramSlider.setOnMouseDragged(event -> {
            ramText.setText(String.format("%s Gb", (int) ramSlider.getValue()));
            ModcraftApplication.launcherConfig.setRam((int) ramSlider.getValue());
        });

        keepLauncherOpen.setSelected(ModcraftApplication.launcherConfig.isKeepOpen());
        keepLauncherOpen.setOnMouseClicked((event) -> {
           ModcraftApplication.launcherConfig.setKeepOpen(keepLauncherOpen.isSelected());
           ModcraftApplication.launcherConfig.save();
        });
    }
}
