package net.sknv.game;

public enum Command {
    SAVESCENE("savescene"),
    LOADSCENE("loadscene"),
    CLEARITEMS("clearitems"),
    REMOVEITEM("removeitem"),
    ROTATEITEM("rotateitem"),
    ADDITEM("additem"),
    ADDCUBES("addcubes");

    private String commandName;
    private String[] arguments;

    Command(String string){
        this.commandName = string;
    };

    Command(String[] input){
        this(input[0]);
        this.arguments = input;
    };

    public String getCommandName() {
        return commandName;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
}
