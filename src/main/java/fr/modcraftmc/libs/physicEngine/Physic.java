package fr.modcraftmc.libs.physicEngine;

import com.sun.javafx.geom.Vec2d;
import fr.modcraftmc.launcher.AsyncExecutor;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Physic {
    static List<Box> scene = new ArrayList<>();
    static List<Box> containers = new ArrayList<>();
    static List<DynamicCollider> dynamicColliders = new ArrayList<>();

    private static boolean running = false;
    private static final float timeStep = 0.01f;
    private static final float borderSize = 1000;
    public static final float baseGravity = 800;

    public static void startEngine(){
        if(running)
            return;

        List<Screen> screens = Screen.getScreens();

        for (Screen screen : screens){
            Rectangle2D bounds = screen.getBounds();
            registerContainer(new Vec2d(bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2), new Vec2d(bounds.getWidth(), bounds.getHeight()));
        }

        running = true;
        AsyncExecutor.runAsync(() -> {
            while(running){
                update(timeStep);
                try {
                    Thread.sleep((long) (timeStep * 1000));
                } catch (InterruptedException ignored) {
                }
            }
        });
    }

    public static void stopEngine(){
        running = false;
    }

    public static void reset(){
        if (running) stopEngine();
        scene.clear();
        synchronized (dynamicColliders){
            dynamicColliders.clear();
        }
    }

    public static DynamicCollider registerDynamicBox(IMovable movable, Vec2d offset, Vec2d size, float bounciness){
        if(running) {
            return null;
        }
        BoxCollider collider = new BoxCollider(offset, size, movable);
        DynamicCollider dynamicCollider = new DynamicCollider(collider);
        dynamicCollider.bounciness = bounciness;
        synchronized (dynamicColliders){
            dynamicColliders.add(dynamicCollider);
        }
        return dynamicCollider;
    }

    public static void registerStaticBox(Vec2d position, Vec2d size){
        if(running) {
            return;
        }
        Box box = new Box(position, size);
        for (Box container : containers){
            if (isColliding(box, container)) {
                return;
            }
        }
        scene.add(box);
    }

    public static void registerContainer(Vec2d position, Vec2d size){
        if(running) {
            return;
        }
        Box containerBox = new Box(position, size);
        containers.add(containerBox);

        List<Box> toRemove = new ArrayList<>();
        for (Box box : scene){
            if (isColliding(box, containerBox)) {
                toRemove.add(box);
            }
        }
        scene.removeAll(toRemove);

        //register 4 boxes for the 4 sides of the container
        registerStaticBox(new Vec2d(position.x, position.y - size.y / 2 - borderSize / 2), new Vec2d(size.x, borderSize));
        registerStaticBox(new Vec2d(position.x, position.y + size.y / 2 + borderSize / 2), new Vec2d(size.x, borderSize));
        registerStaticBox(new Vec2d(position.x - size.x / 2 - borderSize / 2, position.y), new Vec2d(borderSize, size.y));
        registerStaticBox(new Vec2d(position.x + size.x / 2 + borderSize / 2, position.y), new Vec2d(borderSize, size.y));
    }

    private static boolean isColliding(BoxCollider collider){
        for(Box box : scene){
            if(isColliding(collider, box))
                return true;
        }
        return false;
    }

    private static boolean isColliding(BoxCollider collider, Box container){
        return AABB_collision(collider.attachedObject.getPos(), collider.size, container.pos, container.size);
    }

    private static boolean isColliding(Box collider, Box container){
        return AABB_collision(collider.pos, collider.size, container.pos, container.size);
    }

    private static boolean AABB_collision(Vec2d pos1, Vec2d size1, Vec2d pos2, Vec2d size2){
        return pos1.x - size1.x / 2 < pos2.x + size2.x / 2 && pos1.x + size1.x / 2 > pos2.x - size2.x / 2
        && pos1.y-size1.y / 2 < pos2.y + size2.y / 2 && pos1.y + size1.y / 2 > pos2.y - size2.y / 2;
    }

    private static Vec2d get_box_penetration(Vec2d pos1, Vec2d size1, Vec2d pos2, Vec2d size2){
        Vec2d penetration = new Vec2d(0, 0);
        if(pos1.y - size1.y / 2 < pos2.y + size2.y / 2 && pos1.y + size1.y / 2 > pos2.y - size2.y / 2){
            if (pos1.y < pos2.y)
                penetration.y = (pos1.y + size1.y / 2) - (pos2.y - size2.y / 2);
            else
                penetration.y = (pos1.y - size1.y / 2) - (pos2.y + size2.y / 2);
        }
        if (pos1.x - size1.x / 2 < pos2.x + size2.x / 2 && pos1.x + size1.x / 2 > pos2.x - size2.x / 2) {
            if (pos1.x < pos2.x)
                penetration.x = (pos1.x + size1.x / 2) - (pos2.x - size2.x / 2);
            else
                penetration.x = (pos1.x - size1.x / 2) - (pos2.x + size2.x / 2);
        }
        if(Math.abs(penetration.x) < Math.abs(penetration.y))
            return new Vec2d(penetration.x, 0);
        else
            return new Vec2d(0, penetration.y);
    }
    private static Vec2d get_box_penetration(BoxCollider collider, Box solid){
        return get_box_penetration(collider.attachedObject.getPos(), collider.size, solid.pos, solid.size);
    }


    private static void solveConstraints(DynamicCollider collider, Box solid){
        Vec2d penetration = get_box_penetration(collider.collider, solid);
        Vec2d projected_penetration_velocity = projectVector(collider.velocity, penetration);
        collider.velocity = new Vec2d(collider.velocity.x - projected_penetration_velocity.x * (1 + collider.bounciness), collider.velocity.y - projected_penetration_velocity.y * (1 + collider.bounciness));
        collider.collider.attachedObject.setPos(new Vec2d(collider.collider.attachedObject.getPos().x - penetration.x, collider.collider.attachedObject.getPos().y - penetration.y));
    }

    private static void update(float timeStep){
        synchronized (dynamicColliders){
            for (DynamicCollider collider : dynamicColliders) {
                collider.update(timeStep);
                for (Box box : scene) {
                    if (isColliding(collider.collider, box)) {
                        solveConstraints(collider, box);
                    }
                }
            }
        }
    }

    public static double magnitude(Vec2d vector) {
        return Math.sqrt(vector.x * vector.x + vector.y * vector.y);
    }

    public static double dot(Vec2d vector, Vec2d axis) {
        return vector.x * axis.x + vector.y * axis.y;
    }

    public static double project(Vec2d vector, Vec2d axis) {
        double axisMagnitude = magnitude(axis);
        if (axisMagnitude == 0) {
            return 0;
        }
        return dot(vector, axis) / axisMagnitude;
    }

    public static Vec2d normalize(Vec2d vector) {
        double vectorMagnitude = magnitude(vector);
        if (vectorMagnitude == 0) {
            return new Vec2d(0, 0);
        }
        return new Vec2d(vector.x / vectorMagnitude, vector.y / vectorMagnitude);
    }

    public static Vec2d projectVector(Vec2d vector, Vec2d axis) {
        Vec2d normalizedAxis = normalize(axis);
        double projection = project(vector, axis);
        return new Vec2d(projection * normalizedAxis.x, projection * normalizedAxis.y);
    }
}
