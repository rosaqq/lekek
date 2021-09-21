package net.sknv.game;

import net.sknv.engine.entities.HudElement;
import net.sknv.engine.entities.TextItem;

import java.util.*;
import java.util.stream.Collectors;

public class HudTerminal {

    private List<HudElement> elements;
    private TextItem terminalText;
    private TextItem consoleText;
    private Optional<String> suggestion;
    private LinkedList<String> history = new LinkedList();
    private int historyIndex = -1;

    public HudTerminal(TrueType font) {
        this.terminalText = new TextItem(font, Optional.of("/"));
        this.consoleText = new TextItem(font, Optional.empty());
        this.suggestion = Optional.empty();
        this.elements = new ArrayList<>(List.of(terminalText,consoleText));
    }

    public TextItem getTextItem() {
        return terminalText;
    }

    public void setTerminal(String text){
        terminalText.setText(text);
    }

    public TextItem getConsoleText() {
        return consoleText;
    }

    public String getTerminalText() {
        return terminalText.getText().orElse("");
    }

    public void addText(String toAdd) {
        setTerminal(getTerminalText().concat(toAdd));
        suggestCompletion();
    }

    public void backspace() {
        if(getTerminalText().length()>1){
            setTerminal(getTerminalText().substring(0, getTerminalText().length()-1));
            suggestCompletion();
        }
    }

    public String enter() {
        history.push(getTerminalText());
        historyIndex = -1;

        String enter = getTerminalText().substring(1);
        setTerminal("/");
        return enter;
    }

    public void open() {
        suggestCompletion();
    }

    public void previous() {
        if(history.size()>0){
            if(++historyIndex>history.size()-1) historyIndex=history.size()-1;
            setTerminal(history.get(historyIndex));
        }
    }

    public void recent() {
        if(history.size()>0){
            if(--historyIndex<0) historyIndex=0;
            setTerminal(history.get(historyIndex));
        }
    }

    public void suggestCompletion(){
        if(!getTerminalText().isEmpty() && !getTerminalText().equals("/") && !getTerminalText().endsWith(" ")){

            String[] parsed = getTerminalText().substring(1).split(" ");
            if(parsed.length==0) return;

            String toMatch = parsed[parsed.length-1];
            System.out.println("expression to match ->" + toMatch);

            List<String> matches = Arrays.stream(Command.values())
                    .filter(c -> c.getCommandName().startsWith(toMatch))
                    .map(c -> c.getCommandName().replaceFirst(toMatch, ""))
                    .collect(Collectors.toList());

            System.out.println("suggestions ->" + matches);
        }
    }

    public void setConsole(String text) {
        consoleText.setText(text);
    }

    public Collection<? extends HudElement> getElements() {
        return elements;
    }

    public void addConsoleText(String text) {
        consoleText.addText(text);
    }
}
