package net.sknv.game;

import java.util.regex.Pattern;

public enum Command {
    SAVESCENE{
        @Override
        public String getCommandName() { return "savescene"; }

        @Override
        public Pattern getSyntax() {
            return Pattern.compile("savescene (?<sceneName>\\w+)");
        }
    },
    LOADSCENE {
        @Override
        public String getCommandName() {
            return "loadscene";
        }

        @Override
        public Pattern getSyntax() {
            return Pattern.compile("loadscene (?<sceneName>\\w+)");
        }
    },
    CLEARITEMS {
        @Override
        public String getCommandName() {
            return "clearitems";
        }

        @Override
        public Pattern getSyntax() {
            return Pattern.compile("clearitems");
        }
    },
    REMOVEITEM {
        @Override
        public String getCommandName() {
            return "removeitem";
        }

        @Override
        public Pattern getSyntax() {
            return Pattern.compile("removeitem( (?<itemId>\\w+))?");
        }
    },
    ROTATEITEM {
        @Override
        public String getCommandName() {
            return "rotateitem";
        }

        @Override
        public Pattern getSyntax() {
            return Pattern.compile("rotate item (?<rotX>\\d+) (?<rotY>\\d+) (?<rotZ>\\d+)");
        }
    },
    ADDITEM {
        @Override
        public String getCommandName() {
            return "additem";
        }

        @Override
        public Pattern getSyntax() {
            return Pattern.compile("additem (?<meshFileName>\\w+)");
        }
    },
    ADDCUBES {
        @Override
        public String getCommandName() {
            return "addcubes";
        }

        @Override
        public Pattern getSyntax() {
            return Pattern.compile("addcubes (?<numberOfCubes>\\d+)");
        }
    },
    HELP {
        @Override
        public String getCommandName() {
            return "help";
        }

        @Override
        public Pattern getSyntax() {
            return Pattern.compile("help");
        }
    };

    private String[] arguments;

    public abstract String getCommandName();

    public abstract Pattern getSyntax();

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
}
