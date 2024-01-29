package fr.modcraftmc.libs.physicEngine;

import com.sun.javafx.geom.Vec2d;

public class BoxCollider {
    public IMovable attachedObject;
    public Vec2d offset;
    public Vec2d size;

    public BoxCollider(Vec2d offset, Vec2d size, IMovable movable){
        attachedObject = movable;
        this.offset = offset;
        this.size = size;
    }
}
