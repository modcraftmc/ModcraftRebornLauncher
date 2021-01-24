package fr.modcraftmc.launcher.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;
import fr.flowarg.flowupdater.download.json.Mod;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.libs.auth.AccountManager;
import fr.modcraftmc.libs.serverpinger.MinecraftPing;
import fr.modcraftmc.libs.serverpinger.MinecraftPingOptions;
import fr.modcraftmc.libs.serverpinger.MinecraftPingReply;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class SettingsController implements IController {

    @FXML
    public Label playername;
    @FXML public ImageView playerhead;
    @FXML public JFXButton logout;

    @FXML public JFXButton returnbtn;
    @FXML public Button closeBtn;
    @FXML public Rectangle hideBtn;

    @FXML public Label serverstate;
    @FXML public Label playerslabel;

    @FXML public Label label;
    @FXML public ProgressBar progress;
    @FXML public JFXSlider ramSlider;
    @FXML public Label ramText;

    @FXML public JFXCheckBox customPathCheckbox;
    @FXML public TextField customPathValue;
    @FXML public JFXButton fidnBtn;
    @FXML public JFXCheckBox keepOpen;
    private static AuthInfos authInfos;


    public static SettingsController instance;

    public void setup(AuthInfos authInfoss) {
        authInfos = authInfoss;
        playername.setText(authInfos.getUsername());
            try {

                Image image = new Image(new URL("https://minotar.net/avatar/" + authInfos.getUsername()).openStream(), 64, 64, false, false);
                this.playerhead.setImage(image);

            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void initialize() {
        instance = this;

        playerslabel.setText("Na/Na");

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                try {
                    MinecraftPingReply data = new MinecraftPing().getPing(new MinecraftPingOptions().setHostname("vps.modcraftmc.fr").setPort(25565));

                    Platform.runLater(() -> playerslabel.setText(String.format("%s/%s joueurs", data.getPlayers().getOnline(), data.getPlayers().getMax())));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 0, 30000);

        logout.setText("SE DÉCONNECTER");
        returnbtn.setText("RETOUR");
        serverstate.setText("En maintenance");
        label.setText("");
        progress.setVisible(false);
        ramSlider.setMin(4);
        ramSlider.setMax(16);
        System.out.println(ModcraftApplication.launcherConfig.getRam());
        ramSlider.setValue(ModcraftApplication.launcherConfig.getRam());
        ramText.setText("Ram: " + ((int)Math.floor(ramSlider.getValue())));

        customPathCheckbox.setSelected(ModcraftApplication.launcherConfig.getInstanceProperty().isCustomInstance());
        customPathValue.setText(ModcraftApplication.launcherConfig.getInstanceProperty().getCustomInstancePath());


        ramSlider.setOnMouseDragged(event -> {
            ramText.setText("Ram: " + ((int)Math.floor(ramSlider.getValue()+0.5)));
            ModcraftApplication.launcherConfig.setRam(((int)Math.floor(ramSlider.getValue()+0.5)));
        });

        customPathCheckbox.setOnMouseClicked(event -> {
            if (!customPathCheckbox.isSelected()) {
                customPathValue.setText("");
                ModcraftApplication.launcherConfig.setInstanceProperty(new InstanceProperty(false, ""));
            }

        });

        keepOpen.setOnMouseClicked(event -> ModcraftApplication.launcherConfig.setKeepOpen(keepOpen.isSelected()));

        fidnBtn.setOnMouseClicked(event -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select directory");
            fileChooser.setInitialDirectory(FilesManager.INSTANCES_PATH);
            File path = fileChooser.showDialog(ModcraftApplication.getWindow());
            customPathValue.setText(path.getAbsolutePath());

            ModcraftApplication.launcherConfig.setInstanceProperty(new InstanceProperty(true, customPathValue.getText()));

        });


        logout.setOnMouseClicked(event -> {
            AccountManager.logout(ModcraftApplication.launcherConfig.getAccesToken());
            ModcraftApplication.getWindow().setScene(new Scene(Utils.loadFxml("login.fxml")));
        });

        returnbtn.setOnMouseClicked(event -> {
            ModcraftApplication.getWindow().setScene(ModcraftApplication.mainScene);
            ModcraftApplication.launcherConfig.save();
        });

        //window action
        closeBtn.setOnMouseClicked(event -> {
            ModcraftApplication.getWindow().hide();
            ModcraftApplication.launcherConfig.save();
            System.exit(0);
        });

        hideBtn.setOnMouseClicked(event ->  {
            ModcraftApplication.getWindow().hide();
            ModcraftApplication.launcherConfig.save();
        });


    }
}
