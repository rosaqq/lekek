package net.sknv.engine.entities;

import net.sknv.game.TrueType;

import java.awt.*;
import java.util.Optional;

public class TextItem extends HudElement {

    private TrueType font;
    private Optional<String> text;
    private Optional<Color> background;

    public TextItem(TrueType font, Optional<String> text){
        super();
        this.font = font;
        this.text = text;
        this.background = Optional.empty();
        setMesh(font.renderText(text.orElse(" "))); //todo: maybe extend the Optional type to Mesh class? kekw
    }

    public Optional<String> getText() {
        return text;
    }

    public void setText(String text) {
        this.text = Optional.of(text);
        getMesh().deleteBuffers();
        setMesh(font.renderText(text));
    }

    public void addText(String text) {
        this.text.ifPresentOrElse( x-> setText(x.concat("\n"+text)), () -> setText(text));
    }
}
