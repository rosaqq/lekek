package net.sknv.game;

import net.sknv.engine.IHud;
import net.sknv.engine.Window;
import net.sknv.engine.entities.HudElement;
import net.sknv.engine.entities.TextItem;
import net.sknv.engine.graph.Material;
import net.sknv.engine.graph.Mesh;
import net.sknv.engine.graph.OBJLoader;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class Hud implements IHud {

    private final ArrayList<HudElement> hudElements;

    private final TextItem myStatusTextItem;

    private final HudElement compassItem;

    private final HudTerminal terminal;

    public Hud(TrueType font) throws Exception {

        this.myStatusTextItem = new TextItem("+", font);

        this.terminal = new HudTerminal(new TextItem("/", font));

        // Create compass
        Mesh mesh = OBJLoader.loadMesh("/models/compass.obj");
        Material material = new Material();
        material.setAmbientColor(new Vector4f(1, 0, 0, 1));
        mesh.setMaterial(material);
        compassItem = new HudElement(mesh);
        compassItem.setScale(40.0f);
        // Rotate to transform it to screen coordinates
        compassItem.setRotationEuclidean(new Vector3f(0f, 0f, (float)Math.PI));

        //bitmap texture
//        int BITMAP_W = font.getBitMapW();
//        int BITMAP_H = font.getBitMapH();
//
//        float zPos = 0;
//        Mesh myMesh = new Mesh(new float[]{0,0,zPos,0,font.getBitMapH(),zPos,BITMAP_W,BITMAP_H,zPos,BITMAP_W,0,zPos}, new float[]{0,0,0,1,1,1,1,0},new float[0],new int[]{0,1,2,0,2,3}, GL_TRIANGLES);
//        myMesh.setMaterial(new Material(font.getBitMapTexture()));
//        HudElement bitmap = new HudElement(myMesh);

        // Create list that holds the items that compose the HUD
        hudElements = new ArrayList<>(List.of(myStatusTextItem, compassItem));
    }

    public void setStatusText(String statusText) {
        this.myStatusTextItem.setText(statusText);
    }

    public void rotateCompass(float angle) {
        this.compassItem.setRotationEuclidean(new Vector3f(0, 0, (float) Math.PI + angle));
    }

    @Override
    public ArrayList<HudElement> getHudElements() {
        return hudElements;
    }

    public void updateSize(Window window) {//todo: make this not run every update
        Vector3f dif = myStatusTextItem.getMesh().getMax().sub(myStatusTextItem.getMesh().getMin(), new Vector3f()).div(2f);
        this.myStatusTextItem.setPosition(window.getCenter().x - dif.x, window.getCenter().y + dif.y, 0);

        this.compassItem.setPosition(window.getWidth() - 40f, 50f, 0);
        this.terminal.getTextItem().setPosition(0f, window.getHeight()-5f, 0);
    }

    public HudTerminal getTerminal() {
        return terminal;
    }

    public void showTerminal() {
        terminal.open();
        hudElements.add(terminal.getTextItem());
    }

    public void hideTerminal() {
        terminal.close();
        hudElements.remove(terminal.getTextItem());
    }

    public void addElement(HudElement myElement) {
        hudElements.add(myElement);
    }
}
