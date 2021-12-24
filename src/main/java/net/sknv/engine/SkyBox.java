package net.sknv.engine;

import net.sknv.engine.entities.AbstractGameItem;
import net.sknv.engine.graph.*;

public class SkyBox extends AbstractGameItem {

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
    public void render(ShaderProgram shaderProgram) {
        super.render(shaderProgram);
    }
}
