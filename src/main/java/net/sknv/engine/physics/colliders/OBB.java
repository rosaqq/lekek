package net.sknv.engine.physics.colliders;

import net.sknv.engine.entities.Collider;
import net.sknv.engine.graph.Material;
import net.sknv.engine.graph.Mesh;
import net.sknv.engine.graph.MeshUtils;
import net.sknv.engine.graph.ShaderProgram;
import org.joml.Math;
import org.joml.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class OBB extends AABB {

    private Vector3f center;
    private Vector3f x, y, z;

    public OBB(Collider collider) {
        super(collider);

        Vector3f min = collider.getMesh().getMin();
        Vector3f max = collider.getMesh().getMax();
        this.x = new Vector3f(Math.abs(max.x - min.x)/2,0,0);
        this.y = new Vector3f(0,Math.abs(max.y - min.y)/2,0);
        this.z = new Vector3f(0,0,Math.abs(max.z - min.z)/2);
        this.center = new Vector3f(min.x + x.x, min.y + y.y, min.z + z.z);

        Vector4f tc = new Vector4f(center.x, center.y, center.z, 1);
        Vector4f tx = new Vector4f(x.x, x.y, x.z,1);
        Vector4f ty = new Vector4f(y.x, y.y, y.z,1);
        Vector4f tz = new Vector4f(z.x, z.y, z.z,1);

        Matrix4f modelViewMatrix = new Matrix4f();
        modelViewMatrix.identity().translate(collider.getPosition()).scale(collider.getScale()).rotate(collider.getRotation());
        modelViewMatrix.transform(tc);

        modelViewMatrix.identity().rotate(collider.getRotation()).scale(collider.getScale());
        modelViewMatrix.transform(tx);
        modelViewMatrix.transform(ty);
        modelViewMatrix.transform(tz);

        this.x = new Vector3f(tx.x, tx.y, tx.z);
        this.y = new Vector3f(ty.x, ty.y, ty.z);
        this.z = new Vector3f(tz.x, tz.y, tz.z);
        this.center = new Vector3f(tc.x, tc.y, tc.z);
    }

    @Override
    public void translate(Vector3f step) {
        super.translate(step);
        this.center.add(step);
    }

    @Override
    public void rotate(Quaternionf rot){
        super.rotate(rot);

        Vector3f d = new Vector3f();
        center.sub(collider.getPosition(),d);

        Quaternionf finalRot = new Quaternionf();
        Quaternionf inv = new Quaternionf();
        collider.getRotation().invert(inv);
        finalRot.mul(collider.getRotation()).mul(rot).mul(inv);

        d.rotate(finalRot);

        collider.getPosition().add(d, center);

        this.x.rotate(finalRot);
        this.y.rotate(finalRot);
        this.z.rotate(finalRot);
    }

    public Vector3f getCenter() {
        return center;
    }

    public Vector3f getX() {
        return x;
    }

    public Vector3f getY() {
        return y;
    }

    public Vector3f getZ() {
        return z;
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        renderColor.ifPresent( color -> {
            Mesh obbMesh = MeshUtils.generateOBB(renderColor.get(), this);

            shaderProgram.setUniform("material", new Material(color));
            glBindVertexArray(obbMesh.getVaoId());
            glDrawElements(GL_LINES, obbMesh.getVertexCount(), GL_UNSIGNED_INT, 0);

            //restore state
            glBindVertexArray(0);
            glBindTexture(GL_TEXTURE_2D, 0);

            super.render(shaderProgram);
        });
    }
}
