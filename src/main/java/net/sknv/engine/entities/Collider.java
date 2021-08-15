package net.sknv.engine.entities;

import net.sknv.engine.graph.Mesh;
import net.sknv.engine.graph.MeshUtils;
import net.sknv.engine.graph.ShaderProgram;
import net.sknv.engine.graph.WebColor;
import net.sknv.engine.physics.colliders.BoundingBox;
import net.sknv.engine.physics.colliders.OBB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.ObjectInputStream;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Collider extends Phantom {

    //computed
    protected Vector3f force, torque;

    //constant vars
    protected Matrix3f IBody, IBodyInv; //momento inercia
    protected float mass;

    //state vars
    protected Matrix3f R;//rotation
    protected Vector3f P, L;//linear and angular momentum

    //derived
    //protected Matrix3f IInv;//inertia inverter matrix ??
    //protected Vector3f v, w;//linear and angular velocity
    protected Vector3f velocity;

    //other
    private Optional<WebColor> showBB = Optional.empty();
    protected transient BoundingBox boundingBox;
    protected boolean isStatic;

    public Collider(Mesh mesh) {
        super(mesh);
        boundingBox = new OBB(this);
        isStatic = false;
        mass = 1;
        velocity = new Vector3f();

        R = new Matrix3f();
        P = velocity.mul(mass, new Vector3f());
        //IBody = ?;
        //IBody.invert(IBodyInv);
        L = new Vector3f();
    }

    public Vector3f getLinearMomentum(){
        return P;
    }

    public Vector3f getAngularMomentum(){
        return L;
    }

    public Matrix3f getR(){
        return R;
    }

    public Vector3f getVelocity(){
        return P.div(mass, new Vector3f());
    }

    @Override
    public void setRotation(Quaternionf rotation) {
        // calculates diff
        Quaternionf diff = new Quaternionf(getRotation());
        diff.difference(rotation);
        this.boundingBox.rotate(diff);

        super.setRotation(rotation);
    }

    @Override
    public void rotate(Quaternionf rotation) {
        super.rotate(rotation);
        this.boundingBox.rotate(rotation);
    }

    private void readObject(ObjectInputStream inputStream) throws Exception {
        inputStream.defaultReadObject();

        // add BB specific stuff
        setBoundingBox(new OBB(this));
    }

    @Override
    public void translate(Vector3f step) {
        super.translate(step);
        this.boundingBox.translate(step);
    }

    @Override
    public void setPosition(Vector3f position) {
        super.setPosition(position);
        setBoundingBox(new OBB(this));
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public float getMass() {
        return mass;
    }

    @Override
    public void render(ShaderProgram shaderProgram, Matrix4f viewMatrix) {
        super.render(shaderProgram, viewMatrix);

        showBB.ifPresent(
                (x) -> {
                    Mesh aabbMesh = MeshUtils.generateAABB(x, boundingBox);
                    Mesh obbMesh = MeshUtils.generateOBB(x, boundingBox);

                    shaderProgram.setUniform("modelViewMatrix", viewMatrix);

                    //draw meshes
                    shaderProgram.setUniform("material", aabbMesh.getMaterial());
                    glBindVertexArray(aabbMesh.getVaoId());
                    glDrawElements(GL_LINES, aabbMesh.getVertexCount(), GL_UNSIGNED_INT, 0);

                    shaderProgram.setUniform("material", obbMesh.getMaterial());
                    glBindVertexArray(obbMesh.getVaoId());
                    glDrawElements(GL_LINES, obbMesh.getVertexCount(), GL_UNSIGNED_INT, 0);

                    //restore state
                    glBindVertexArray(0);
                    glBindTexture(GL_TEXTURE_2D, 0);
                    showBB = Optional.empty();
                }
        );
    }

    public void setBbColor(WebColor color) {
        this.showBB = Optional.of(color);
    }

    @Override
    public String toString() {
        return "Collider{" +
                "boundingBox=" + boundingBox +
                '}';
    }

    public void applyForce(Vector3f force) {
        this.force.add(force);
    }
}
