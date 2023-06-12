package fr.modcraftmc.libs.physicEngine;

import com.sun.javafx.geom.Vec2d;
import fr.modcraftmc.launcher.Utils;
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
    private static Thread engineThread;

    public static void startEngine(){
        if(running)
            return;

        List<Screen> screens = Screen.getScreens();

        for (Screen screen : screens){
            Rectangle2D bounds = screen.getBounds();
            registerContainer(new Vec2d(bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2), new Vec2d(bounds.getWidth(), bounds.getHeight()));
        }

        running = true;
        engineThread = new Thread(() -> {
            while(running){
                update(timeStep);
                try {
                    Thread.sleep((long) (timeStep * 1000));
                } catch (InterruptedException ignored) {
                }
            }
        });

        engineThread.start();
    }

    public static void stopEngine(){
        running = false;
    }

    public static void reset(){
        if (running) stopEngine();
        scene.clear();
        dynamicColliders.clear();
    }

    public static DynamicCollider registerDynamicBox(IMovable movable, Vec2d offset, Vec2d size, float bounciness){
        if(running) {
            return null;
        }
        BoxCollider collider = new BoxCollider(offset, size, movable);
        DynamicCollider dynamicCollider = new DynamicCollider(collider);
        dynamicCollider.bounciness = bounciness;
        dynamicColliders.add(dynamicCollider);
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
        for (Iterator<Box> iterator = scene.iterator(); iterator.hasNext();){
            Box box = iterator.next();
            if (isColliding(box, containerBox)) {
                scene.remove(box);
            }
        }
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
        Vec2d projected_penetration_velocity = Utils.projectVector(collider.velocity, penetration);
        collider.velocity = new Vec2d(collider.velocity.x - projected_penetration_velocity.x * (1 + collider.bounciness), collider.velocity.y - projected_penetration_velocity.y * (1 + collider.bounciness));
        collider.collider.attachedObject.setPos(new Vec2d(collider.collider.attachedObject.getPos().x - penetration.x, collider.collider.attachedObject.getPos().y - penetration.y));
    }

    private static void update(float timeStep){
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
