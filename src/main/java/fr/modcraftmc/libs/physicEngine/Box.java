package fr.modcraftmc.libs.physicEngine;

import com.sun.javafx.geom.Vec2d;

public class Box {
    Vec2d pos;
    Vec2d size;

    public Box(Vec2d pos, Vec2d size){
        this.pos = pos;
        this.size = size;
    }
}
