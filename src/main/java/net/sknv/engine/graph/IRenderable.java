package net.sknv.engine.graph;

import org.joml.Matrix4f;

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
     * @param shaderProgram {@link ShaderProgram} for this object.
     * @param transformationResult Transformation matrix for this object.
     */
    void render(ShaderProgram shaderProgram, Matrix4f transformationResult);
}
