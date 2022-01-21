package net.sknv.game;

import net.sknv.engine.GameEngine;

public class Main {

    public static void main(String[] args) {
        try {
            Terminal terminal = new Terminal();
            terminal.setDaemon(true);

            boolean vsync = true;
            GameLogic gameLogic = new GameLogic();
            GameEngine gameEngine = new GameEngine("Ultimate Kek Game", 600, 480, vsync, gameLogic, terminal);

            terminal.start();
            gameEngine.run();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
