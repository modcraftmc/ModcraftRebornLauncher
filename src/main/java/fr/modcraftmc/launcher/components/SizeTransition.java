package fr.modcraftmc.launcher.components;

import javafx.animation.Transition;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class SizeTransition extends Transition {
    private Region node;


    private double startWidth;
    private double startHeight;

    private double endWidth;
    private double endHeight;

    public SizeTransition(Duration duration, Region node) {
        setCycleDuration(duration);
        setCycleCount(1);
        this.node = node;
    }

    public void setFromValues(double startWidth, double startHeight) {
        this.startWidth = startWidth;
        this.startHeight = startHeight;
    }

    public void setToValues(double endWidth, double endHeight) {
        this.endWidth = endWidth;
        this.endHeight = endHeight;
    }

    @Override
    protected void interpolate(double frac) {
        node.setPrefHeight(startHeight + (endHeight - startHeight) * frac);
        node.setPrefWidth(startWidth + (endWidth - startWidth) * frac);
    }
}
