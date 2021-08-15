package net.sknv.game;

import java.util.Scanner;

public class Terminal extends Thread {

    private String input = null;

    public Terminal(){}

    private synchronized void setInput(String input){
        this.input = input;
    }

    public void run(){
        Scanner s = new Scanner(System.in);
        String in;

        while (true) {
            in = s.nextLine();
            setInput(in);
        }
    }

    public synchronized String getInput(){
        if (input == null){
            return null;
        } else {
            String temp = input;
            input = null;
            return temp;
        }
    }
}
