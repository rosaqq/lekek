package net.sknv.engine.entities;

import net.sknv.engine.graph.Mesh;
import net.sknv.engine.graph.ShaderProgram;

public class HudElement extends AbstractGameItem {

    public HudElement() {
        super();
    }

    public HudElement(Mesh mesh) {
        super(mesh);
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        super.render(shaderProgram);
    }
}
