package net.sknv.engine.entities;

import net.sknv.engine.graph.Mesh;
import net.sknv.engine.graph.ShaderProgram;
import net.sknv.engine.physics.colliders.BoundingBox;
import net.sknv.engine.physics.colliders.OBB;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.ObjectInputStream;

public class Collider extends Phantom {

    //computed
    protected Vector3f force, toque;

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
    protected transient BoundingBox boundingBox;
    protected boolean isStatic;

    public Collider(Mesh mesh) {
        super(mesh);
        boundingBox = new OBB(this);
        isStatic = false;
        mass = 1;
        velocity = new Vector3f();
        //IBody = ?;
        //IBody.invert(IBodyInv);
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
        return velocity;
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
    public void render(ShaderProgram shaderProgram) {
        super.render(shaderProgram);
    }

    @Override
    public String toString() {
        return "Collider{" +
                "boundingBox=" + boundingBox +
                '}';
    }
}
