package net.sknv.game;

import net.sknv.engine.IHud;
import net.sknv.engine.Window;
import net.sknv.engine.entities.HudElement;
import net.sknv.engine.entities.TextItem;
import net.sknv.engine.graph.FontTexture;
import net.sknv.engine.graph.Material;
import net.sknv.engine.graph.Mesh;
import net.sknv.engine.graph.OBJLoader;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Hud implements IHud {

    private static final Font FONT = new Font("Arial", Font.PLAIN, 20);

    private static final String CHARSET = "ISO-8859-1";

    private final ArrayList<HudElement> hudElements;

    private final TextItem statusTextItem;

    private final HudElement compassItem;

    private final HudTerminal terminal;

    public Hud(String statusText) throws Exception {
        FontTexture fontTexture = new FontTexture(FONT, CHARSET);
        this.statusTextItem = new TextItem(statusText, fontTexture);
        this.statusTextItem.getMesh().getMaterial().setAmbientColor(new Vector4f(1, 1, 1, 1));

        this.terminal = new HudTerminal(new TextItem("/", fontTexture));

        // Create compass
        Mesh mesh = OBJLoader.loadMesh("/models/compass.obj");
        Material material = new Material();
        material.setAmbientColor(new Vector4f(1, 0, 0, 1));
        mesh.setMaterial(material);
        compassItem = new HudElement(mesh);
        compassItem.setScale(40.0f);
        // Rotate to transform it to screen coordinates
        compassItem.setRotationEuclidean(new Vector3f(0f, 0f, (float)Math.PI));

        // Create list that holds the items that compose the HUD
        hudElements = new ArrayList<>(List.of(statusTextItem, compassItem));
    }

    public void setStatusText(String statusText) {
        this.statusTextItem.setText(statusText);
    }

    public void rotateCompass(float angle) {
        this.compassItem.setRotationEuclidean(new Vector3f(0, 0, (float) Math.PI + angle));
    }

    @Override
    public ArrayList<HudElement> getHudElements() {
        return hudElements;
    }

    public void updateSize(Window window) {
        this.statusTextItem.setPosition(window.getCenter().x, window.getCenter().y, 0);
        this.compassItem.setPosition(window.getWidth() - 40f, 50f, 0);
        this.terminal.getTextItem().setPosition(0f, window.getHeight()-20f, 0);
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
