package net.sknv.game;

import java.util.List;
import java.util.Optional;

public enum Command {
    SAVESCENE{
        @Override
        public String getCommandName() { return "savescene"; }

        @Override
        public Optional<List<List<Object>>> getSyntax() {
            return Optional.of(List.of(List.of("sceneName")));
        }
    },
    LOADSCENE {
        @Override
        public String getCommandName() {
            return "loadscene";
        }

        @Override
        public Optional<List<List<Object>>> getSyntax() {
            return Optional.of(List.of(List.of("sceneName")));
        }
    },
    CLEARITEMS {
        @Override
        public String getCommandName() {
            return "clearitems";
        }

        @Override
        public Optional<List<List<Object>>> getSyntax() {
            return Optional.empty();
        }
    },
    REMOVEITEM {
        @Override
        public String getCommandName() {
            return "removeitem";
        }

        @Override
        public Optional<List<List<Object>>> getSyntax() {
            return Optional.of(List.of(
                    List.of(Optional.of("objectId")),
                    List.of())
            );
        }
    },
    ROTATEITEM {
        @Override
        public String getCommandName() {
            return "rotateitem";
        }

        @Override
        public Optional<List<List<Object>>> getSyntax() {
            return Optional.of(List.of(List.of("rotX", "rotY", "rotZ")));
        }
    },
    ADDITEM {
        @Override
        public String getCommandName() {
            return "additem";
        }

        @Override
        public Optional<List<List<Object>>> getSyntax() {
            return Optional.of(List.of(List.of("meshName")));
        }
    },
    ADDCUBES {
        @Override
        public String getCommandName() {
            return "addcubes";
        }

        @Override
        public Optional<List<List<Object>>> getSyntax() {
            return Optional.of(List.of(List.of("numberOfCubes")));
        }
    },
    HELP {
        @Override
        public String getCommandName() {
            return "help";
        }

        @Override
        public Optional<List<List<Object>>> getSyntax() {
            return Optional.empty();
        }
    };

    private String[] arguments;

    public abstract String getCommandName();

    public abstract Optional<List<List<Object>>> getSyntax();

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
}
