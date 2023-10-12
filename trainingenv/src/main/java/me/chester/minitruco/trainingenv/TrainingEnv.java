package me.chester.minitruco.trainingenv;

import py4j.GatewayServer;

public class TrainingEnv {

    public static String init() {
        return "Java diz: Olá, mundo!";
    }

    public int addition(int first, int second) {
      return first + second;
    }

    public static void main(String[] args) {
        TrainingEnv app = new TrainingEnv();
        // app is now the gateway.entry_point
        GatewayServer server = new GatewayServer(app);
        server.start();
    }
}
