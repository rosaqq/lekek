package net.sknv.game;

import java.util.Optional;
import java.util.Scanner;

public class Terminal extends Thread {

    private Optional<String> input = Optional.empty();

    public Terminal(){}

    private synchronized void setInput(String input){
        this.input = Optional.of(input);
    }

    public void run(){
        Scanner s = new Scanner(System.in);
        String in;

        while (true) {
            in = s.nextLine();
            setInput(in);
        }
    }

    public synchronized Optional<String> getInput(){
        return input;
    }
}
