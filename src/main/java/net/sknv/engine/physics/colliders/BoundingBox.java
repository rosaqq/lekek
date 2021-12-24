package net.sknv.engine.physics.colliders;

import net.sknv.engine.entities.Collider;
import net.sknv.engine.graph.IRenderable;
import net.sknv.engine.graph.WebColor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;

public abstract class BoundingBox implements IRenderable {
    public abstract void translate(Vector3f v);
    public abstract void rotate(Quaternionf rot);
    public abstract void setRenderColor(Optional<WebColor> color);
    public abstract Collider getCollider();
    public abstract EndPoint getMin();
    public abstract EndPoint getMax();
}
