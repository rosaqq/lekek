package net.sknv.engine.entities;

import net.sknv.engine.graph.Mesh;
import net.sknv.engine.graph.ShaderProgram;

public class Phantom extends AbstractGameItem {

    public Phantom(Mesh mesh) {
        super(mesh);
    }
    @Override
    public void render(ShaderProgram shaderProgram) {
        super.render(shaderProgram);
    }
}
