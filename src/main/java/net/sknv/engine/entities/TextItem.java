package net.sknv.engine.entities;

import net.sknv.game.TrueType;

import java.util.Optional;

public class TextItem extends HudElement {

    private TrueType font;
    private Optional<String> text;

    public TextItem(Optional<String> text, TrueType font){
        super();
        this.font = font;
        this.text = text;

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
}
