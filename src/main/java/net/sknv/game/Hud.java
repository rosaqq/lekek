package net.sknv.game;

import net.sknv.engine.Window;
import net.sknv.engine.entities.HudElement;
import net.sknv.engine.entities.TextItem;
import net.sknv.engine.graph.*;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Hud {

	private static final WebColor CROSS_HAIR_COLOR = WebColor.White;
	private final ArrayList<IRenderable> hudElements;
	private final TextItem crossHair;
	private final HudElement compassItem;

	private final HudTerminal terminal;

	public Hud(TrueType font) throws Exception {

		this.crossHair = new TextItem(font, Optional.of("+"), CROSS_HAIR_COLOR);

		this.terminal = new HudTerminal(font);

		// Create compass
		Mesh mesh = OBJLoader.loadMesh("/models/compass.obj");
		Material material = new Material();
		material.setAmbientColor(new Vector4f(1, 0, 0, 1));
		compassItem = new HudElement(mesh, material);
		compassItem.setScale(40.0f);
		// Rotate to transform it to screen coordinates
		compassItem.setRotationEuclidean(new Vector3f(0f, 0f, (float) Math.PI));

		//bitmap texture
//        int BITMAP_W = font.getBitMapW();
//        int BITMAP_H = font.getBitMapH();
//
//        float zPos = 0;
//        Mesh myMesh = new Mesh(new float[]{0,0,zPos,0,font.getBitMapH(),zPos,BITMAP_W,BITMAP_H,zPos,BITMAP_W,0,zPos}, new float[]{0,0,0,1,1,1,1,0},new float[0],new int[]{0,1,2,0,2,3}, GL_TRIANGLES);
//        myMesh.setMaterial(new Material(font.getBitMapTexture()));
//        HudElement bitmap = new HudElement(myMesh);

		// Create list that holds the items that compose the HUD
		hudElements = new ArrayList<IRenderable>(List.of(crossHair, compassItem));
	}

	public void setStatusText(String statusText) {
		this.crossHair.setText(statusText);
	}

	public void rotateCompass(float angle) {
		this.compassItem.setRotationEuclidean(new Vector3f(0, 0, (float) Math.PI + angle));
	}

	public ArrayList<IRenderable> getHudElements() {
		return hudElements;
	}

	public void updateSize(Window window) {//todo: make this not run every update
		Vector3f dif = crossHair.getMesh().getMax().sub(crossHair.getMesh().getMin(), new Vector3f()).div(2f);
		crossHair.setPosition(window.getCenter().x - dif.x, window.getCenter().y + dif.y - crossHair.getMesh().getMax().y, 0);

		Vector3f consoleSize = terminal.getConsoleText().getMesh().getMax().sub(terminal.getConsoleText().getMesh().getMin(), new Vector3f());

		compassItem.setPosition(window.getWidth() - 40f, 50f, 0f);
		terminal.getTextItem().setPosition(0f, window.getHeight() - 5f, 0f);
		terminal.getConsoleText().setPosition(0f, terminal.getTextItem().getPosition().y - consoleSize.y - 5f, 0f);
	}

	public HudTerminal getTerminal() {
		return terminal;
	}

	public void showTerminal() {
		terminal.open();
		hudElements.addAll(terminal.getElements());
	}

	public void hideTerminal() {
		hudElements.removeAll(terminal.getElements());
	}

	public void addElement(HudElement myElement) {
		hudElements.add(myElement);
	}

	public void cleanup() {
		for (IRenderable element : getHudElements()) element.cleanup();
	}
}
