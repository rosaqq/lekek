package net.sknv.engine.entities;

import net.sknv.engine.graph.Material;
import net.sknv.engine.graph.WebColor;
import net.sknv.game.TrueType;

import java.util.Optional;

public class TextItem extends Phantom{

	private final TrueType font;
	private Optional<String> text;

	public TextItem(TrueType font, Optional<String> text, WebColor color) {
		super(font.getMeshFor(text.orElse(" ")), new Material(font.getBitMapTexture()));
		this.font = font;
		this.text = text;
	}

	public Optional<String> getText() {
		return text;
	}

	public void setText(String text) {
		this.text = Optional.of(text);
		super.setMesh(font.getMeshFor(text));
	}

	public void addText(String text) {
		this.text.ifPresentOrElse(x -> setText(x.concat("\n" + text)), () -> setText(text));
	}
}
