package net.sknv.engine.entities;

import net.sknv.engine.graph.Mesh;
import net.sknv.engine.graph.ShaderProgram;
import net.sknv.engine.graph.Texture;
import net.sknv.engine.graph.Transformation;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class HudElement extends AbstractGameItem {

    public HudElement() {
        super();
    }

    public HudElement(Mesh mesh) {
        super(mesh);
    }

    @Override
    public void render(ShaderProgram shaderProgram, Matrix4f orthoProjMatrix) {

        int drawMode = GL_TRIANGLES;

        // Set ortohtaphic and model matrix for this HUD item
        Matrix4f projModelMatrix = Transformation.getOrtoProjModelMatrix(this, orthoProjMatrix);

        shaderProgram.setUniform("projModelMatrix", projModelMatrix);
        shaderProgram.setUniform("colour", mesh.getMaterial().getAmbientColor());
        shaderProgram.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);

        Texture texture = mesh.getMaterial().getTexture();
        if (texture != null) {
            //tell openGL to use first texture bank and bind texture
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture.getId());
            //glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
        else {
            //for test models
            //glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
        }

        //draw mesh
        glBindVertexArray(mesh.getVaoId());

        glDrawElements(drawMode, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

        //restore state
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
