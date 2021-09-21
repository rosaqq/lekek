package net.sknv.game;

public enum Command {
    SAVESCENE{
        @Override
        public String getCommandName() { return "savescene"; }
    },
    LOADSCENE {
        @Override
        public String getCommandName() {
            return "loadscene";
        }
    },
    CLEARITEMS {
        @Override
        public String getCommandName() {
            return "clearitems";
        }
    },
    REMOVEITEM {
        @Override
        public String getCommandName() {
            return "removeitem";
        }
    },
    ROTATEITEM {
        @Override
        public String getCommandName() {
            return "rotateitem";
        }
    },
    ADDITEM {
        @Override
        public String getCommandName() {
            return "additem";
        }
    },
    ADDCUBES {
        @Override
        public String getCommandName() {
            return "addcubes";
        }
    },
    HELP {
        @Override
        public String getCommandName() {
            return "help";
        }
    };

    private String[] arguments;

    public abstract String getCommandName();

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
}
