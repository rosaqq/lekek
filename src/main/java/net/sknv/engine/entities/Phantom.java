package net.sknv.engine.entities;

import net.sknv.engine.graph.IRenderable;
import net.sknv.engine.graph.Mesh;
import net.sknv.engine.graph.ShaderProgram;
import net.sknv.engine.graph.Texture;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Phantom implements IRenderable {

    protected Mesh mesh;
    protected Vector3f position;
    protected Quaternionf rotation;
    protected float scale;

    public Phantom(Mesh mesh) {
        this.mesh = mesh;
        this.position = new Vector3f();
        this.rotation = new Quaternionf();
        this.scale = 1;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setPosition(float x, float y, float z) {
        setPosition(new Vector3f(x,y,z));
    }

    public void setPosition(Vector3f position){
        this.position = position;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    /**
     * Rotates object *to* the specified Euler Angles.
     * @param eulerAngle Euler Angles representing the *final state* of the object in reference to the world axis.
     */
    public void setRotationEuclidean(Vector3f eulerAngle) {
        setRotation(new Quaternionf().rotationXYZ(eulerAngle.x, eulerAngle.y, eulerAngle.z));
    }
    /**
     * Rotates object *to* the specified Quaternion.
     * @param rotation A Quaternion representing the *final state* of the object in reference to the world axis.
     */
    public void setRotation(Quaternionf rotation){
        this.rotation = rotation;
    }

    /**
     * Rotates object *by* the specified Euler Angles.
     * @param eulerAngle Euler Angles representing the rotation increment.
     */
    public void rotateEuclidean(Vector3f eulerAngle) {
        rotate(new Quaternionf().rotationXYZ(eulerAngle.x, eulerAngle.y, eulerAngle.z));
    }
    /**
     * Rotates object *by* the specified rotation.
     * @param rotation A Quaternion representing the rotation increment.
     */
    public void rotate(Quaternionf rotation){
        this.rotation.mul(rotation);
    }

    /**
     * Rotates object *by* the specified Euler Angle in the *World Axis*.
     * @param eulerAngle Euler Angles representing the rotation increment in reference to the *World Axis*
     */
    public void rotateWorldEuclidean(Vector3f eulerAngle) {
        rotateWorld(new Quaternionf().rotationXYZ(eulerAngle.x, eulerAngle.y, eulerAngle.z));
    }
    /**
     * Rotates object *by* the specified rotation in the *World Axis*.
     * @param rotation Quaternion representing the rotation increment in reference to the *World Axis*
     */
    public void rotateWorld(Quaternionf rotation) {
        //resets rotation, applies global first, then local
        setRotation(new Quaternionf().mul(rotation).mul(this.rotation));
    }

    public void render(ShaderProgram shaderProgram) {
        Texture texture = mesh.getMaterial().getTexture();
        if (texture != null) {
            //tell openGL to use first texture bank and bind texture
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }
        else {
            // glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }

        //draw mesh
        glBindVertexArray(mesh.getVaoId());

        glDrawElements(mesh.getDrawMode(), mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

        //restore state
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void translate(Vector3f step) {
        this.position.add(step);
    }

    @Override
    public String toString() {
        return "Phantom{" +
                "position=" + position +
                ", rotation=" + rotation +
                ", scale=" + scale +
                ", mesh=" + mesh +
                '}';
    }

}
