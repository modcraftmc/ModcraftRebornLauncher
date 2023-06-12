package fr.modcraftmc.libs.physicEngine;

import com.sun.javafx.geom.Vec2d;

public class DynamicCollider {
    public BoxCollider collider;
    public Vec2d velocity;
    public Vec2d acceleration;
    public float bounciness;

    public DynamicCollider(BoxCollider collider){
        this.collider = collider;
        velocity = new Vec2d(0, 0);
        acceleration = new Vec2d(0, Physic.baseGravity);
    }

    public void update(float timeStep){
        velocity = new Vec2d(velocity.x + acceleration.x * timeStep, velocity.y + acceleration.y * timeStep);
        Vec2d initialPos = collider.attachedObject.getPos();
        collider.attachedObject.setPos(new Vec2d(initialPos.x + velocity.x * timeStep, initialPos.y + velocity.y * timeStep));
    }
}
