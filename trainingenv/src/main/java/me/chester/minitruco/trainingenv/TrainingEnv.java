package me.chester.minitruco.trainingenv;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.JogadorBot;
import me.chester.minitruco.core.PartidaLocal;
import me.chester.minitruco.core.SituacaoJogo;
import py4j.GatewayServer;

public class TrainingEnv {

    public static void main(String[] args) {
        TrainingEnv app = new TrainingEnv();
        // app is now the gateway.entry_point
        GatewayServer server = new GatewayServer(app);
        server.start();
        System.out.println("Servidor de env de treinamento iniciado");
    }

    public Episodio novoEpisodio() {
        return new Episodio();
    }

    private static class Episodio {

        private PartidaLocal partida;
        private JogadorAgente jogadorAgente;
        public boolean vezDoAgente;

        /**
         * Cria uma nova partida contra três bots
         */
        public Episodio() {
            partida = new PartidaLocal(false, false, "P");
            jogadorAgente = new JogadorAgente(this);
            partida.adiciona(jogadorAgente);
            partida.adiciona(new JogadorBot());
            partida.adiciona(new JogadorBot());
            partida.adiciona(new JogadorBot());
            vezDoAgente = false;
            (new Thread(partida)).start();
        }

        /**
         * Aguarda a vez do agente e retorna o estado atual
         *
         * @return estado do agente (string separada por espaços)
         */
        public String proximoEstado() {
            while (!vezDoAgente) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return jogadorAgente.situacaoJogo.toString();
        }

        public void joga(int indiceCarta) {
            partida.jogaCarta(jogadorAgente, jogadorAgente.situacaoJogo.cartasJogador[indiceCarta]);
            vezDoAgente = false;
        }

        public void finaliza() {
            partida.abandona(jogadorAgente.getPosicao());
        }
    }

    private static class JogadorAgente extends Jogador {

        public SituacaoJogo situacaoJogo = new SituacaoJogo();
        private Episodio episodio;

        public JogadorAgente(Episodio episodio) {
            super();
            this.episodio = episodio;
        }

        @Override
        public void cartaJogada(Jogador j, Carta c) {

        }

        @Override
        public void inicioMao(Jogador jogadorQueAbre) {
            System.out.println("iniciomao");
        }

        @Override
        public void inicioPartida(int placarEquipe1, int placarEquipe2) {
            System.out.println("iniciopartida");

        }

        @Override
        public void vez(Jogador j, boolean podeFechada) {
            if (j == this) {
                System.out.println("vez do agente");

                // TODO eu acho que isso vai sempre colocar as 3 cartas,
                //      ver se é um problema
                situacaoJogo.cartasJogador = getCartas();
                partida.atualizaSituacao(situacaoJogo, this);
                // TODO implementar
//                if (partida.isPlacarPermiteAumento()) {
//                    situacaoJogo.valorProximaAposta = valorProximaAposta;
//                } else {
//                    situacaoJogo.valorProximaAposta = 0;
//                }
                episodio.vezDoAgente = true;
            }
        }

        @Override
        public void pediuAumentoAposta(Jogador j, int valor, int rndFrase) {

        }

        @Override
        public void aceitouAumentoAposta(Jogador j, int valor, int rndFrase) {

        }

        @Override
        public void recusouAumentoAposta(Jogador j, int rndFrase) {

        }

        @Override
        public void rodadaFechada(int numRodada, int resultado, Jogador jogadorQueTorna) {

        }

        @Override
        public void maoFechada(int[] pontosEquipe) {

        }

        @Override
        public void jogoFechado(int numEquipeVencedora, int rndFrase) {

        }

        @Override
        public void decidiuMaoDeX(Jogador j, boolean aceita, int rndFrase) {

        }

        @Override
        public void informaMaoDeX(Carta[] cartasParceiro) {

        }

        @Override
        public void jogoAbortado(int posicao, int rndFrase) {

        }
    }
}
