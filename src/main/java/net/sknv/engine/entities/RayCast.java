package net.sknv.engine.entities;

import org.joml.Vector3f;

public class RayCast {
    public Vector3f origin;
    public Vector3f direction;

    private static final double EPSILON = 0.0000001;

    public RayCast(Vector3f origin, Vector3f direction) {
        this.origin = origin;
        this.direction = direction.normalize();
    }

    public Vector3f intersectPlane(Vector3f origin, Vector3f normal){
        Vector3f rayToQuad = new Vector3f();
        origin.sub(this.origin, rayToQuad);

        float k = rayToQuad.dot(normal) / this.direction.dot(normal);

        if(k>=0 && k<=1){//reaches plane
            Vector3f intersectionPoint = new Vector3f();
            direction.mul(k, intersectionPoint);
            intersectionPoint.add(this.origin);
            return intersectionPoint;
        } else if(k>=1){//doesnt reach the plane needs scaling (ofc it needs! kinda dumb method but yea we'll see later)
            Vector3f intersectionPoint = new Vector3f();
            direction.mul(k, intersectionPoint);
            intersectionPoint.add(this.origin);
            return intersectionPoint;
        } //else opposite direction
        return null;
    }

    public boolean intersectsTriangle(Vector3f vertex0, Vector3f vertex1, Vector3f vertex2){
        Vector3f edge1 = new Vector3f();
        Vector3f edge2 = new Vector3f();
        Vector3f h = new Vector3f();
        Vector3f s = new Vector3f();
        Vector3f q = new Vector3f();
        double a, f, u, v;
        vertex1.sub(vertex0, edge1);
        vertex2.sub(vertex0, edge2);

        direction.cross(edge2, h);
        a = edge1.dot(h);
        if (a > -EPSILON && a < EPSILON) {
            return false;    // Ray is parallel
        }
        f = 1.0 / a;
        origin.sub(vertex0, s);
        u = f * (s.dot(h));
        if (u < 0.0 || u > 1.0) {
            return false;
        }
        s.cross(edge1, q);
        v = f * direction.dot(q);
        if (v < 0.0 || u + v > 1.0) {
            return false;
        }
        // At this stage we can compute t to find out where the intersection point is on the line.
        double t = f * edge2.dot(q);
        if (t > EPSILON) // ray intersection
        {
            System.out.println("distance hit = " + t);
            return true;
        } else // This means that there is a line intersection but not a ray intersection.
        {
            return false;
        }
    }

    public boolean intersectsItem(Collider collider) {
        Vector3f min = new Vector3f(collider.getBoundingBox().getMin().getPosition());
        Vector3f max = new Vector3f(collider.getBoundingBox().getMax().getPosition());

        Vector3f[] vertex = new Vector3f[]{
                min, new Vector3f(max.x, min.y, min.z), new Vector3f(min.x, min.y, max.z),
                min, new Vector3f(min.x, max.y, min.z), new Vector3f(min.x, min.y, max.z),
                min, new Vector3f(min.x, max.y, min.z), new Vector3f(max.x, min.y, min.z),

                max, new Vector3f(max.x, max.y, min.z), new Vector3f(min.x, max.y, max.z),
                max, new Vector3f(max.x, min.y, max.z), new Vector3f(min.x, max.y, max.z),
                max, new Vector3f(max.x, min.y, max.z), new Vector3f(max.x, max.y, min.z),

                new Vector3f(min.x, max.y, min.z), new Vector3f(max.x, max.y, min.z), new Vector3f(min.x, max.y, max.z),
                new Vector3f(min.x, max.y, min.z), new Vector3f(max.x, max.y, min.z), new Vector3f(max.x, min.y, min.z),
                new Vector3f(min.x, max.y, min.z), new Vector3f(min.x, max.y, max.z), new Vector3f(min.x, min.y, max.z),

                new Vector3f(max.x, min.y, max.z), new Vector3f(max.x, min.y, min.z), new Vector3f(min.x, min.y, max.z),
                new Vector3f(max.x, min.y, max.z), new Vector3f(max.x, min.y, min.z), new Vector3f(max.x, max.y, min.z),
                new Vector3f(max.x, min.y, max.z), new Vector3f(min.x, min.y, max.z), new Vector3f(min.x, max.y, max.z),
        };

        for(int i=0; i!=vertex.length; i+=3){
            if(intersectsTriangle(vertex[i], vertex[i+1], vertex[i+2])) return true;
        }
        return false;
    }
}