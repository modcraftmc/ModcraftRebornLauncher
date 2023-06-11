package fr.modcraftmc.launcher.components;

import animatefx.animation.AnimateFXInterpolator;
import animatefx.animation.AnimationFX;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * @author Loïc Sculier aka typhon0
 */
public class FadeOutWithDuration extends AnimationFX {
    /**
     * Create a new FadeOut animation
     * @param node the node to affect
     */
    public FadeOutWithDuration(Node node) {
        super(node);
    }

    @Override
    protected AnimationFX resetNode() {
        getNode().setOpacity(1);
        return this;
    }

    @Override
    protected void initTimeline() {
        setTimeline(new Timeline(
                new KeyFrame(Duration.millis(0),
                        new KeyValue(getNode().opacityProperty(), 1, AnimateFXInterpolator.EASE)
                ),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(getNode().opacityProperty(), 0, AnimateFXInterpolator.EASE)
                )
        ));
    }
}
