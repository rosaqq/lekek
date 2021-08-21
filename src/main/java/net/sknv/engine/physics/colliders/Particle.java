package net.sknv.engine.physics.colliders;

import org.joml.Vector3f;

/**
 * A Particle consists of the simplest object that can be simulated by the physics engine.
 */
public class Particle {

    /**
     * Linear position of particle in world space
     */
    private Vector3f position;

    /**
     * Linear velocity of particle in world space
     */
    private Vector3f velocity;

    /**
     * Acceleration of the particle.
     */
    private Vector3f acceleration;

    /**
     * Holds the amount of damping applied to linear
     * motion. Damping is required to remove energy added
     * through numerical instability in the integrator.
     */
    private float damping;

    /**
     * Inverse of the mass of the particle.
     */
    private float inverseMass;

    public Particle() {
        this.position = new Vector3f();
        this.velocity = new Vector3f();
        this.acceleration = new Vector3f();
    }

    /**
     * Integrates the particle forward in time by the given amount.
     * This function uses a Newton-Euler integration method, which is a
     * linear approximation of the correct integral. For this reason it
     * may be inaccurate in some cases.
     */
    public void integrate(float duration){
        // Update linear position.
        position.add(velocity.mul(duration, new Vector3f()));

        // Work out the acceleration from the force.
        Vector3f resultingAcc = acceleration;
        resultingAcc.add(forceAccum.mul(inverseMass, new Vector3f()));

        // Update linear velocity from the acceleration.
        velocity.add(resultingAcc.mul(duration), new Vector3f());

        // Impose drag.
        velocity.mul((float) Math.pow(damping, duration));
    }

}
