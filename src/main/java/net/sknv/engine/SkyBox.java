package net.sknv.engine;

import net.sknv.engine.entities.AbstractGameItem;
import net.sknv.engine.graph.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class SkyBox extends AbstractGameItem {

    private Matrix4f projectionMatrix;
    private Vector3f ambientLight;

    public SkyBox(String objModel, String textureFile) throws Exception {
        //todo problem: SKYBOX NOT WORKING WHEN SERIALIZED
        super();
        Mesh skyBoxMesh = OBJLoader.loadMesh(objModel);
        Texture skyBoxTexture = new Texture(textureFile);
        skyBoxMesh.setMaterial(new Material(skyBoxTexture, 0));
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);

    }

    /**
     * <b>DO NOT</b> call this method before passing the Projection Matrix and the Ambient Light via:
     * <ul>
     *     <li>skyBox.setAmbientLight(...)</li>
     *     <li>skyBox.setProjectionMatrix(...)</li>
     * </ul>
     * // todo: fix this behaviour
     * <br>
     * Check parent method for full documentation.
     */
    @Override
    public void render(ShaderProgram shaderProgram, Matrix4f viewMatrix) throws AssertionError {

        int drawMode = GL_TRIANGLES;

        // this part used to be on Mesh::render
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

        glDrawElements(drawMode, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

        //restore state
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
