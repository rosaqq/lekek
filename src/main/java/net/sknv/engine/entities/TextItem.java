package net.sknv.engine.entities;

import net.sknv.game.TrueType;

public class TextItem extends HudElement {

    private TrueType font;
    private String text;

    public TextItem(String text, TrueType font){
        super();
        this.text = text;
        this.font = font;
        setMesh(font.renderText(text));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        getMesh().deleteBuffers();
        setMesh(font.renderText(text));
    }
}
