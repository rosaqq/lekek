package net.sknv.engine.graph;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Implement this for renderable behaviour.
 */
public interface IRenderable {

    /**
     * This method gets called inside the Renderer class to draw the object it belongs to.<br><br>
     * The requirements for rendering an object are:<br>
     * <ol>
     *   <li>A shader program</li>
     *   <li>A transformation matrix. This can be the ViewMatrix, ModelViewMatrix or OrthoProjModelMatrix, for example.</li>
     * </ol>
     * In our case, the required transformations are applied inside this method's implementations and so we pass it the
     * first matrix in the sequence: the View Matrix (or the OrthoProjMatrix for the HUD case).
     * @param shaderProgram {@link ShaderProgram} for this object.
     */
    void render(ShaderProgram shaderProgram);

    Vector3f getPosition();

    float getScale();

    Quaternionf getRotation();

    Material getMaterial();

    Mesh getMesh();

    void cleanup();

    boolean isTextured();
}
