package net.sknv.engine;

import net.sknv.engine.entities.Phantom;
import net.sknv.engine.graph.Material;
import net.sknv.engine.graph.OBJLoader;
import net.sknv.engine.graph.Texture;

public class SkyBox extends Phantom {

	public SkyBox(String objModel, String textureFile) throws Exception { //todo: deprecate this class?
		super(OBJLoader.loadMesh(objModel), new Material(new Texture(textureFile), 0));
	}
}
