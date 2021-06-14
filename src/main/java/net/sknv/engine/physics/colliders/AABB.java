package net.sknv.engine.physics.colliders;

import net.sknv.engine.entities.Collider;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;

public class AABB implements BoundingBox {

    public Collider collider;
    public EndPoint min, max;

    public AABB(Collider collider) {
        this.collider = collider;

        //load vertices
        ArrayList<Vector3f> vertices = collider.getMesh().getVertices();
        ArrayList<Vector3f> tvertices = new ArrayList<>();

        //transform vertices according to gameItem state
        Matrix4f modelViewMatrix = new Matrix4f();
        modelViewMatrix.identity().translate(collider.getPosition()).scale(collider.getScale()).rotate(collider.getRotation());

        for (Vector3f v : vertices){
            Vector4f tv = new Vector4f(v.x, v.y, v.z, 1);
            modelViewMatrix.transform(tv);
            tvertices.add(new Vector3f(tv.x, tv.y, tv.z));
        }

        this.min = new EndPoint(this, tvertices.get(0), true);
        this.max = new EndPoint(this, tvertices.get(7), false);

        //find new min and max
        for (Vector3f v : tvertices){
            if (v.x < min.getX()) min.setX(v.x);
            if (v.y < min.getY()) min.setY(v.y);
            if (v.z < min.getZ()) min.setZ(v.z);

            if (v.x > max.getX()) max.setX(v.x);
            if (v.y > max.getY()) max.setY(v.y);
            if (v.z > max.getZ()) max.setZ(v.z);
        }
    }
    public EndPoint getMin() {
        return min;
    }

    public EndPoint getMax() {
        return max;
    }

    public void translate(Vector3f step){
        this.min.getPosition().add(step);
        this.max.getPosition().add(step);
    }

    @Override
    public void rotate(Quaternionf rot) {
        //calculate new AABB
        ArrayList<Vector3f> vertices = collider.getMesh().getVertices();
        ArrayList<Vector3f> tvertices = new ArrayList<>();

        Matrix4f modelViewMatrix = new Matrix4f();
        modelViewMatrix.identity().translate(collider.getPosition()).scale(collider.getScale()).rotate(collider.getRotation());

        for (Vector3f v : vertices){
            Vector4f tv = new Vector4f(v.x, v.y, v.z, 1);
            modelViewMatrix.transform(tv);
            tvertices.add(new Vector3f(tv.x, tv.y, tv.z));
        }

        min.setPosition(new Vector3f(tvertices.get(0)));
        max.setPosition(new Vector3f(tvertices.get(7)));

        for (Vector3f v : tvertices){
            if (v.x < min.getX()) min.setX(v.x);
            if (v.y < min.getY()) min.setY(v.y);
            if (v.z < min.getZ()) min.setZ(v.z);

            if (v.x > max.getX()) max.setX(v.x);
            if (v.y > max.getY()) max.setY(v.y);
            if (v.z > max.getZ()) max.setZ(v.z);
        }
    }

    @Override
    public Collider getCollider() {
        return collider;
    }

    public String toString() {
        return "min " + min.getPosition().x + "," + min.getPosition().y + "," + min.getPosition().z +
                "\tmax " + max.getPosition().x + "," + max.getPosition().y + "," + max.getPosition().z;
    }
}
