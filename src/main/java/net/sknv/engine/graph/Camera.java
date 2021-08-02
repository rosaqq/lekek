package net.sknv.engine.graph;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Camera {

    private final Vector3f position;
    private final Quaternionf rotation;

    public Camera(Vector3f position, Quaternionf rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        if ( offsetZ != 0 ) {
            position.x += (float)Math.sin(rotation.y) * -1.0f * offsetZ;
            position.z += (float)Math.cos(rotation.y) * offsetZ;
        }
        if ( offsetX != 0) {
            position.x += (float)Math.sin(rotation.y - (Math.PI/2)) * -1.0f * offsetX;
            position.z += (float)Math.cos(rotation.y - (Math.PI/2)) * offsetX;
        }
        position.y += offsetY;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        rotation.x = x;
        rotation.y = y;
        rotation.z = z;
    }

    public void moveRotation(Vector3f rotInc) {
        rotation.premul(new Quaternionf().rotationX(rotInc.x));
        rotation.mul(new Quaternionf().rotationY(rotInc.y));
    }
}