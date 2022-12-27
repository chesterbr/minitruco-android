package me.chester.minitruco.server;

import me.chester.minitruco.core.Baralho;

public class MiniTrucoServer {

    public static void main(String[] args) {
        Baralho b = new Baralho(true);
        System.out.println("Uma carta: " + b.sorteiaCarta());
    }

}