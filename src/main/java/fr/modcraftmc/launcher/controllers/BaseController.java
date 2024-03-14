package fr.modcraftmc.launcher.controllers;

import com.sun.javafx.geom.Vec2d;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.libs.physicEngine.DynamicCollider;
import fr.modcraftmc.libs.physicEngine.IMovable;
import fr.modcraftmc.libs.physicEngine.Physic;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;

public abstract class BaseController implements IController, IMovable {

    private Pane pane;
    private double xOffset = 0;
    private double yOffset = 0;

    private double lastCursorX = 0;
    private double lastCursorY = 0;

    private float distanceDragged;
    private boolean funStarted = false;
    private DynamicCollider dynamicCollider;
    @FXML public Pane closeButton;
    @FXML public Pane minimiseButton;

    @Override
    public void initialize(FXMLLoader loader) {
        pane = loader.getRoot();

        closeButton.setOnMouseClicked(event -> {
            ModcraftApplication.shutdown(0);
        });

        minimiseButton.setOnMouseClicked(event -> {
            ModcraftApplication.getWindow().setIconified(true);
        });

        pane.setOnMousePressed(event -> {
            lastCursorX = xOffset = event.getSceneX();
            lastCursorY = yOffset = event.getSceneY();
            distanceDragged = 0;
        });
        pane.setOnMouseDragged(event -> {
            if(!funStarted) {
                if (distanceDragged > 5555 && distanceDragged / 5 > 5 * 5 * 5 * 55) {
                    Platform.runLater(this::startFun);
                    distanceDragged = 5;
                }
                float currentDistanceDragged = (float) (Math.abs(event.getScreenX() - lastCursorX) + Math.abs(event.getScreenY() - lastCursorY));
                distanceDragged += currentDistanceDragged;
                Utils.pleaseWait(555).thenRun(() -> {
                    distanceDragged -= currentDistanceDragged;
                });
            }
            else {
                dynamicCollider.velocity = new Vec2d((event.getScreenX() - lastCursorX) * 300, (event.getScreenY() - lastCursorY) * 300);
            }

            ModcraftApplication.getWindow().setX(event.getScreenX() - xOffset);
            ModcraftApplication.getWindow().setY(event.getScreenY() - yOffset);
            lastCursorX = event.getScreenX();
            lastCursorY = event.getScreenY();
        });
    }

    @Override
    public void setPos(Vec2d pos) {
        ModcraftApplication.getWindow().setX(pos.x - ModcraftApplication.getWindow().getWidth() / 2);
        ModcraftApplication.getWindow().setY(pos.y - ModcraftApplication.getWindow().getHeight() / 2);
    }

    @Override
    public Vec2d getPos() {
        return new Vec2d(ModcraftApplication.getWindow().getX() + ModcraftApplication.getWindow().getWidth() / 2, ModcraftApplication.getWindow().getY() + ModcraftApplication.getWindow().getHeight() / 2);
    }

    private void startFun() {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Don't show this to anyone, this is a secret feature !\nYou will now experience a lot of fun !");
        alert.setHeaderText("WARNING !!!");
        alert.setTitle("ModcraftMC");
        alert.showAndWait();
        dynamicCollider = Physic.registerDynamicBox(this, new Vec2d(0, 0), new Vec2d(ModcraftApplication.getWindow().getWidth(), ModcraftApplication.getWindow().getHeight()), 0.5f);
        setPos(new Vec2d(Screen.getPrimary().getBounds().getWidth() / 2, Screen.getPrimary().getBounds().getHeight() / 2));
        Physic.startEngine();
        funStarted = true;
    }
}
