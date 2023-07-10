package me.chester.minitruco.android.multiplayer.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */
public class JogadorBluetooth extends Jogador implements Runnable {

    private final static Logger LOGGER = Logger.getLogger("JogadorBluetooth");

    private InputStream in;
    private final BluetoothSocket socket;
    private final ServidorBluetoothActivity servidor;

    public JogadorBluetooth(BluetoothSocket socket,
            ServidorBluetoothActivity servidor) {
        this.socket = socket;
        this.servidor = servidor;
        new Thread(this).start();
    }

    /**
     * Processa as mensagens vindas do cliente (i.e., do JogoBT no celular
     * remoto), transformando-as novamente em eventos na PartidaLocal.
     */
    public void run() {
        // Aguarda a definição da posição (importante, pois ela determina o slot
        // no servidor para o envio de mensagens)
        while (getPosicao() == 0) {
            Thread.yield();
        }
        // Caractere lido e buffer que acumula a linha lida
        int c;
        StringBuffer sbLinha = new StringBuffer();
        try {
            in = socket.getInputStream();
            // O loop dura enquanto o InputStream não for null. Sim, eu poderia
            // usar uma leitura mais eficiente, com blocking, mas aí não consigo
            // detectar o fim da partida (sem perder o primeiro caractere do
            // primeiro comando da partida seguinte)
            do {
                while (in != null && in.available() == 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // Alguém já tratou isso um dia?
                    }
                }
                if (in == null)
                    break;
                // Lê o próximo caractre
                c = in.read();
                if (c != BaseBluetoothActivity.SEPARADOR_REC) {
                    // Acumula caracteres até formar uma linha
                    sbLinha.append((char) c);
                } else {
                    // Processa linhas (não-vazias)
                    if (sbLinha.length() > 0) {
                        LOGGER.log(Level.INFO,
                                "Linha acumulada: " + sbLinha);
                        char tipoNotificacao = sbLinha.charAt(0);
                        String[] args = sbLinha.toString().split(" ");
                        switch (tipoNotificacao) {
                        case 'J':
                            // Procura a carta correspondente ao parâmetro
                            Carta[] cartas = getCartas();
                            for (Carta carta : cartas) {
                                if (carta != null
                                        && carta.toString().equals(args[1])) {
                                    carta.setFechada(args.length > 2
                                            && args[2].equals("T"));
                                    partida.jogaCarta(this, carta);
                                }
                            }
                            break;
                        case 'H':
                            partida.decideMaoDeX(this, args[1].equals("T"));
                            break;
                        case 'T':
                            partida.aumentaAposta(this);
                            break;
                        case 'D':
                            partida.respondeAumento(this, true);
                            break;
                        case 'A':
                            partida.abandona(getPosicao());
                            break;
                        case 'C':
                            partida.respondeAumento(this, false);
                            break;
                        }
                        sbLinha.setLength(0);
                    }
                }
            } while (in != null);
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Exceção no jogador (fim de jogo?)", e);
            // Não precisa tratar - ou é fim de jogo, ou o servidor cuida
        }
        LOGGER.log(Level.INFO, "encerrando loop JogadorBT");
    }

    /**
     * Encerra a thread principal, efetivamente finalizando o JogadorBT
     */
    void finaliza() {
        in = null;
    }

    /**
     * Manda uma linha de texto para o celular do cliente.
     *
     * @param linha representa um evento gerado pela PartidaLocal.
     */
    public synchronized void enviaLinha(String linha) {
        servidor.enviaLinha(getPosicao() - 2, linha);
    }

    // Os métodos restantes convertem as notificações da PartidaLocal em mensagens
    // de texto, que serão reconvertidas em solicitações no cliente para o
    // JogadorHumano.

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
        enviaLinha("J " + j.getPosicao() + param);
    }

    public void inicioMao(Jogador jogadorQueAbre) {
        StringBuffer comando = new StringBuffer("M");
        comando.append(" ").append(jogadorQueAbre.getPosicao());
        for (int i = 0; i <= 2; i++)
            comando.append(" ").append(getCartas()[i]);
        // Se for manilha nova, também envia o "vira"
        if (!partida.getModo().isManilhaVelha()) {
            comando.append(" ").append(partida.cartaDaMesa);
        }
        enviaLinha(comando.toString());
    }

    public void inicioPartida(int p1, int p2) {
        enviaLinha("P");
    }

    public void vez(Jogador j, boolean podeFechada) {
        enviaLinha("V " + j.getPosicao() + ' ' + (podeFechada ? 'T' : 'F'));
    }

    public void pediuAumentoAposta(Jogador j, int valor, int rndFrase) {
        enviaLinha("T " + j.getPosicao() + ' ' + valor + ' ' + rndFrase);
    }

    public void aceitouAumentoAposta(Jogador j, int valor, int rndFrase) {
        enviaLinha("D " + j.getPosicao() + ' ' + valor + ' ' + rndFrase);
    }

    public void recusouAumentoAposta(Jogador j, int rndFrase) {
        enviaLinha("C " + j.getPosicao() + ' ' + rndFrase);
    }

    public void rodadaFechada(int numRodada, int resultado,
            Jogador jogadorQueTorna) {
        enviaLinha("R " + resultado + ' ' + jogadorQueTorna.getPosicao());
    }

    public void maoFechada(int[] pontosEquipe) {
        enviaLinha("O " + pontosEquipe[0] + ' ' + pontosEquipe[1]);
    }

    public void decidiuMaoDeX(Jogador j, boolean aceita, int rndFrase) {
        enviaLinha("H " + j.getPosicao() + (aceita ? " T" : " F") + ' ' + rndFrase);
    }

    public void informaMaoDeX(Carta[] cartasParceiro) {
        StringBuffer sbComando = new StringBuffer("F ");
        for (int i = 0; i <= 2; i++) {
            sbComando.append(cartasParceiro[i]);
            if (i != 2)
                sbComando.append(' ');
        }
        enviaLinha(sbComando.toString());
    }

    // Eventos de fim-de-jogo

    public void jogoFechado(int numEquipeVencedora, int rndFrase) {
        enviaLinha("G " + numEquipeVencedora + " " + rndFrase);
        finaliza();
    }

    public void jogoAbortado(int posicao, int rndFrase) {
        enviaLinha("A " + posicao + ' ' + rndFrase);
        finaliza();
    }

}
