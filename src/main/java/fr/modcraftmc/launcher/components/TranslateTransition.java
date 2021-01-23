package fr.modcraftmc.launcher.components;

import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.util.Duration;

public class TranslateTransition extends Transition {

    private Node node;

    public TranslateTransition(Duration duration, Node node) {

        this.setCycleDuration(duration);
        this.setCycleCount(1);

        this.node = node;

    }


    @Override
    protected void interpolate(double frac) {

        if ((node.getTranslateX() + 5 * frac) >= 115) return;
        node.setTranslateX(node.getTranslateX() + 5 * frac);

    }

}
