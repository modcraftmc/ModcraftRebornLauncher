package fr.modcraftmc.libs.physicEngine;

import com.sun.javafx.geom.Vec2d;

public class BoxCollider {
    IMovable attachedObject;
    Vec2d offset;
    Vec2d size;

    public BoxCollider(Vec2d offset, Vec2d size, IMovable movable){
        attachedObject = movable;
        this.offset = offset;
        this.size = size;
    }
}
