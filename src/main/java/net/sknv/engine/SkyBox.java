package net.sknv.engine;

import net.sknv.engine.entities.Phantom;
import net.sknv.engine.graph.Material;
import net.sknv.engine.graph.OBJLoader;
import net.sknv.engine.graph.Texture;

public class SkyBox extends Phantom {

    public SkyBox(String objModel, String textureFile) throws Exception {
        super(OBJLoader.loadMesh(objModel));
        Texture skyBoxTexture = new Texture(textureFile);
        mesh.setMaterial(new Material(skyBoxTexture, 0));
    }
}
