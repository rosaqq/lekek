package net.sknv.game;

import net.sknv.engine.entities.TextItem;

public class HudTerminal {
    private TextItem terminalText;
    private String stored = "";
    private String previous = null;

    public HudTerminal(TextItem terminalText) {
        this.terminalText = terminalText;
    }

    public TextItem getTextItem() {
        return terminalText;
    }

    public void setText(String text){
        terminalText.setText(text);
    }

    public String getText() {
        return terminalText.getText();
    }

    public void addText(String toAdd) {
        setText(getText().concat(toAdd));
        stored = getText();
    }

    public void backspace() {
        if(getText().length()>1){
            setText(getText().substring(0,getText().length()-1));
            stored = getText();
        }
    }

    public String enter() {
        previous = getText();
        String enter = getText().substring(1);
        stored = "";
        setText("/");
        return enter;
    }

    public void close() {
        stored = getText();
        setText("/");
    }

    public void open() {
        if(!stored.isEmpty()) setText(stored);
        else setText("/");
    }

    public void previous() {
        if(previous!=null){
            setText(previous);
        }
    }
}
