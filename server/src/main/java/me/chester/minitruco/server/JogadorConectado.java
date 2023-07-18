package me.chester.minitruco.server;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

/**
 * Representa um cliente conectado, dentro ou fora de uma partida.
 * <p>
 * A classe é capaz de processar os comandos do jogador, e, uma vez associada à
 * partida, interagir com ela.
 */
public class JogadorConectado extends Jogador implements Runnable {

    /**
     * Nomes de jogadores online (para evitar duplicidade)
     */
    private static final Set<String> nomes = new HashSet<>();
    private final Socket cliente;

    /**
     * Informa se o jogador está participando de uma partida
     */
    public boolean jogando = false;
    /**
     * Informa se o jogador autorizou o início da partida na sala
     */
    public boolean querJogar = false;

    private Sala sala;

    /**
     * Buffer de saída do jogador (para onde devemos "printar" os resultados dos
     * comandos)
     */
    private PrintStream out;

    /**
     * Cria um novo jogador
     *
     * @param cliente socket-cliente através do qual o jogador se conectou
     */
    public JogadorConectado(Socket cliente) {
        this.cliente = cliente;
    }

    /**
     * Verifica se um nome está em uso por algum jogador
     *
     * @param nome nome a verificar
     * @return true se já está em uso, false caso contrário
     */
    public static boolean isNomeEmUso(String nome) {
        return nomes.contains(nome.toUpperCase());
    }

    /**
     * Impede que um nome seja usado
     *
     */
    public static void bloqueiaNome(String nome) {
        nomes.add(nome.toUpperCase());
    }

    /**
     * Libera o uso de um nome
     *
     */
    public static void liberaNome(String nome) {
        nomes.remove(nome.toUpperCase());
    }

    // TODO: ver se não vai fazer falta
    // @Override
    // public void jogadorAceito(Jogador j, Partida partida) {
    // println("Y " + j.getPosicao());
    // }

    /**
     * Envia uma linha de texto para o cliente (tipicamente o resultado de um
     * comando)
     *
     * @param linha linha de texto a enviar
     */
    public void println(String linha) {
        out.print(linha);
        out.print("\r\n");
        if (linha.length() > 0) {
            ServerLogger.evento(this, linha);
        }
    }

    /**
     * Aguarda comandos do jogador e os executa
     */
    public void run() {
        ServerLogger.evento(this, "conectou");
        try {
            // Configura um timeout para evitar conexões presas
            ServerLogger.evento(this, "timeout antes:" + cliente.getSoTimeout());
            cliente.setSoTimeout(10000);
            ServerLogger.evento(this, "timeout depois:" + cliente.getSoTimeout());
            // Prepara o buffer de saída
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    cliente.getInputStream()));
            out = new PrintStream(cliente.getOutputStream());
            // Imprime info do servidor (como mensagem de boas-vindas)
            (new ComandoW()).executa(null, this);
            String linha = "";
            while (linha != null) {
                try {
                    linha = in.readLine();
                    Comando.interpreta(linha, this);
                } catch (SocketTimeoutException e) {
                    // A linha é só pra garantir que, no caso de uma conexão presa
                    // o teste abaixo dela dê erro (seja no if, seja exception no print)
                    println("");
                    if (!cliente.isConnected()) {
                        ServerLogger.evento("Desconexao detectada durante timeout");
                        return;
                    }
                    continue;
                }
            }
        } catch (IOException e) {
            // Meio improvável de rolar, however...
            ServerLogger.evento(e, "Erro de I/O no loop principal do jogador");
        } finally {
            // Ao final, remove o usuário de qualquer sala em que esteja,
            // remove seu nome da lista de nomes usados e loga
            if (getSala() != null) {
                (new ComandoS()).executa(null, this);
            }
            if (!getNome().equals("unnamed")) {
                liberaNome(getNome());
            }
            ServerLogger.evento(this, "desconectou");
        }

    }



    @Override
    public void cartaJogada(Jogador j, Carta c) {
        String param;
        if (c.isFechada()) {
            if (j.equals(this)) {
                param = " " + c + " T";
            } else {
                param = "";
            }
        } else {
            param = " " + c;
        }
        println("J " + j.getPosicao() + param);
    }

    @Override
    public void inicioMao(Jogador jogadorQueAbre) {
        StringBuilder comando = new StringBuilder("M");
        comando.append(" ").append(jogadorQueAbre.getPosicao());
        for (int i = 0; i <= 2; i++)
            comando.append(" ").append(getCartas()[i]);
        if (!partida.getModo().isManilhaVelha()) {
            comando.append(" ").append(partida.cartaDaMesa);
        }
        println(comando.toString());
    }

    @Override
    public void inicioPartida(int placarEquipe1, int placarEquipe2) {
        // TODO comparar com bluetooth (todos os eventos, alias)
        println("P " + getPosicao());
    }

    @Override
    public void vez(Jogador j, boolean podeFechada) {
        println("V " + j.getPosicao() + ' ' + (podeFechada ? 'T' : 'F'));
    }

    @Override
    public void pediuAumentoAposta(Jogador j, int valor, int rndFrase) {
        println("T " + j.getPosicao() + ' ' + valor + ' ' + rndFrase);
    }

    @Override
    public void aceitouAumentoAposta(Jogador j, int valor, int rndFrase) {
        println("D " + j.getPosicao() + ' ' + valor + ' ' + rndFrase);
    }

    @Override
    public void recusouAumentoAposta(Jogador j, int rndFrase) {
        println("C " + j.getPosicao() + ' ' + rndFrase);
    }

    @Override
    public void rodadaFechada(int numRodada, int resultado,
                              Jogador jogadorQueTorna) {
        println("R " + resultado + ' ' + jogadorQueTorna.getPosicao());
    }

    @Override
    public void maoFechada(int[] pontosEquipe) {
        println("O " + pontosEquipe[0] + ' ' + pontosEquipe[1]);
    }

    @Override
    public void jogoFechado(int numEquipeVencedora, int rndFrase) {
        desvinculaJogo();
        println("G " + numEquipeVencedora + " " + rndFrase);
    }

    @Override
    public void decidiuMaoDeX(Jogador j, boolean aceita, int rndFrase) {
        println("H " + j.getPosicao() + (aceita ? " T" : " F") + ' ' + rndFrase);
    }

    @Override
    public void informaMaoDeX(Carta[] cartasParceiro) {
        StringBuilder sbComando = new StringBuilder("F ");
        for (int i = 0; i <= 2; i++) {
            sbComando.append(cartasParceiro[i]);
            if (i != 2)
                sbComando.append(' ');
        }
        println(sbComando.toString());
    }

    @Override
    public void jogoAbortado(int posicao, int rndFrase) {
        desvinculaJogo();
        println("A " + posicao);
    }

    /**
     * Desvincula a partida do jogador, e, se necessário, da sala
     */
    private synchronized void desvinculaJogo() {
        querJogar = false;
        jogando = false;
        Sala s = getSala();
        if (s != null)
            s.liberaJogo();
    }

    /**
     * Recupera a sala em que o jogado restá
     *
     * @return objeto representando a sala, ou null se estiver fora de uma sala
     */
    public Sala getSala() {
        return sala;
    }

    /**
     * Associa o jogador com a sala. NÃO deve ser usado diretamente (ao invés disso,
     * use Sala.adiciona() e Sala.remove())
     */
    public void setSala(Sala sala) {
        this.sala = sala;
    }

    /**
     * Atribui um nome ao jogador (apenas se não houver outro com o mesmo nome)
     */
    @Override
    public synchronized void setNome(String nome) {
        // Se já existir, desencana
        if (isNomeEmUso(nome)) {
            return;
        }
        // Se já tinha um nome, libera o seu uso
        if (!this.getNome().equals("unnamed")) {
            liberaNome(this.getNome());
        }
        // Seta o novo nome e evita novos usos
        super.setNome(nome);
        bloqueiaNome(nome);
    }

    public String getIp() {
        return cliente.getInetAddress().getHostAddress();
    }

}
