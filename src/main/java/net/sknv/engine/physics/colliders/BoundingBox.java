package net.sknv.engine.physics.colliders;

import net.sknv.engine.entities.Collider;
import net.sknv.engine.graph.ShaderProgram;
import net.sknv.engine.graph.WebColor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;

public abstract class BoundingBox { //todo: REFACTOR THIS interface and AABB/OBB
    public abstract void translate(Vector3f v);
    public abstract void rotate(Quaternionf rot);
    public abstract void setRenderColor(Optional<WebColor> color); //todo spaghet
    public abstract Collider getCollider();
    public abstract EndPoint getMin(); //todo: remove getMin and getMax methods
    public abstract EndPoint getMax(); //these methods are aabb specific
    public abstract void render(ShaderProgram shaderProgram);
}
