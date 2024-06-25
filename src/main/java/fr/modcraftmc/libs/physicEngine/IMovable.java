package fr.modcraftmc.libs.physicEngine;

import com.sun.javafx.geom.Vec2d;

public interface IMovable {
    void setPos(Vec2d pos);

    Vec2d getPos();
}
