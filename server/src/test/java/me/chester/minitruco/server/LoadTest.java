package me.chester.minitruco.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class LoadTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("iniciando LoadTest");
        int porta = 6912;

        if (args.length != 2) {
            System.out.println("passe o servidor e o # de conexões");
            return;
        }
        String host = args[0];
        int numConexoes = Integer.parseInt(args[1]);
        Set<Thread> threads = new HashSet<>();

        for (int i = 0; i < numConexoes; i++) {
            Thread.sleep(1);
            threads.add(Thread.ofVirtual().start(() -> {
                try {
                    Socket socket = new Socket(host, porta);
                    System.out.println("Conectado " + host + ":" + porta);
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                            socket.getOutputStream())), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                    out.println("N robozinho");
                    out.println("E PUB P");
                    String linha = in.readLine();
                    while ((linha = in.readLine()) != null) {
                        System.out.println("Recebido: " + linha);
                        if (linha.startsWith("K")) {
                            out.println(linha);
                            System.out.println("Mandou keepalive");
                        } else if (linha.endsWith("1 PUB") && !linha.contains("bot")) {
                            out.println("Q");
                        }
                    }

                    socket.close();
                } catch (IOException e) {
                    System.out.println("Erro ao conectar em " + host + ":" + porta + ": " + e.getMessage());
                }
            }));
        }

        System.out.println("Todo mundo entrou, agora eles ficam até sair!");
        for (Thread t : threads) {
            t.join();
        }
        System.out.println("Todos saíram, fim.");
    }
}
