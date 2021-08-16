package net.sknv.engine.physics;

import net.sknv.engine.Scene;
import net.sknv.engine.entities.Collider;
import net.sknv.engine.physics.colliders.BoundingBox;
import net.sknv.engine.physics.collisionDetection.SPCollision;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PhysicsEngine {

    private SPCollision sweepPrune;

    public PhysicsEngine() {
        sweepPrune = new SPCollision();
    }

    public void addGameItem(Collider collider) {
        sweepPrune.addItem(collider);
    }

    public void simulate(Scene scene){
        ArrayList<Collider> colliders = scene.getColliders();
        updateState(colliders);
        //detectCollisions(gameItems);
        //solveCollisions();
    }

    private void updateState(Collection< ? extends Collider> colliders) {
        Vector3f velocity;
        Matrix3f Iinv;
        Vector3f angularVel = new Vector3f();

        for (Collider collider : colliders){
            velocity = collider.getVelocity();

            Iinv = collider.getR().mul(collider.getIBodyInv(), new Matrix3f()).mul(collider.getR().transpose(new Matrix3f()));
            collider.getAngularMomentum().mul(Iinv, angularVel); //maybe wrong

            collider.setPosition(collider.getPosition().add(velocity)); // x = x + v
            collider.rotate(new Quaternionf(angularVel.x, angularVel.y, angularVel.z,1).normalize().mul(collider.getRotation())); //q' = 1/2*w*q maybe wrong
            collider.setLinearMomentum(collider.getLinearMomentum().add(collider.getF())); //p = p + f
            collider.setAngularMomentum(collider.getAngularMomentum().add(collider.getT())); //l = l + t
        }
    }

    private void detectCollisions(Collider collider) {
        //calculate step
        Vector3f step = collider.getVelocity().mul(0.1f);

        Set<BoundingBox> col = sweepPrune.checkStepCollisions(collider, step);

        if (col.isEmpty()) {
            //no collisions, perform movement
            collider.translate(step);
            collider.getVelocity().zero();
        } else {
            handleCollisions(collider, col);
        }
    }

    private void handleCollisions(Collider collider, Set<BoundingBox> bbx){
        Vector3f step = collider.getVelocity().mul(0.1f); // CHANGE THIS! step should be calculated from gameitem properties thus gameitem.getStep
        Vector3f possibleStep = calculatePossibleStep(collider, bbx.iterator().next(), step); //could calculate for every item but cba as for now
        Vector3f intersection = new Vector3f();
        Vector3f nIntersection = new Vector3f();

        step.sub(possibleStep, intersection);

        Set<BoundingBox> staticItems = new HashSet<>();
        for(BoundingBox bb : bbx) { //define possible step and remove components from intersect
            if (!bb.getCollider().isStatic()) {
                //static collider
                staticItems.add(bb);
                Set<BoundingBox> colX = null, colY = null, colZ = null;

                colX = sweepPrune.checkStepCollisions(collider, new Vector3f(step.x, 0, 0));
                colY = sweepPrune.checkStepCollisions(collider, new Vector3f(0, step.y, 0));
                colZ = sweepPrune.checkStepCollisions(collider, new Vector3f(0, 0, step.z));

                if (colX.isEmpty() && colY.isEmpty() && colZ.isEmpty()) {
                    //collision onto static no movement
                    return;
                }

                if (!colX.isEmpty()) intersection.x = 0; else possibleStep.x = step.x;
                if (!colY.isEmpty()) intersection.y = 0; else possibleStep.y = step.y;
                if (!colZ.isEmpty()) intersection.z = 0; else possibleStep.z = step.z;
            }
        }

        //we should have possible step and intersection calculated
        bbx.removeAll(staticItems);
        intersection.div(bbx.size(), nIntersection);

        //if all collider were static
        if(bbx.isEmpty()){
            //perform movement
            collider.translate(possibleStep);
            collider.getVelocity().zero();
            return;
        }

        //apply nInt to all movable colliders
        for(BoundingBox bbCol : bbx) {
            //calculate resulting effect based on velocity , item materials and nIntersect
            collisionResponse(collider, bbCol.getCollider(), step, nIntersection);
        }

    }

    private void collisionResponse(Collider agent, Collider subject, Vector3f step, Vector3f intersect){
        float agentP = intersect.length() * agent.getMass();
        float subjectP = intersect.length() * subject.getMass();

        if(agentP >= subjectP) ;
    }

    private Vector3f calculatePossibleStep(Collider collider, BoundingBox bb, Vector3f step) {
        float x = 0;
        float y = 0;
        float z = 0;

        if(step.x>0) x = bb.getMin().getX() - collider.getBoundingBox().getMax().getX();
        if(step.x<0) x = bb.getMax().getX() - collider.getBoundingBox().getMin().getX();

        if(step.y>0) y = bb.getMin().getY() - collider.getBoundingBox().getMax().getY();
        if(step.y<0) y = bb.getMax().getY() - collider.getBoundingBox().getMin().getY();


        if(step.z>0) z = bb.getMin().getZ() - collider.getBoundingBox().getMax().getZ();
        if(step.z<0) z = bb.getMax().getZ() - collider.getBoundingBox().getMin().getZ();

        Vector3f intersection = new Vector3f(x,y,z);
        step.sub(intersection, intersection);

        return intersection;
    }
}
