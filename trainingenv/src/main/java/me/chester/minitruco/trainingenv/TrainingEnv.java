package me.chester.minitruco.trainingenv;

import java.util.concurrent.Callable;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Estrategia;
import me.chester.minitruco.core.JogadorBot;
import me.chester.minitruco.core.PartidaLocal;
import me.chester.minitruco.core.SituacaoJogo;
import py4j.GatewayServer;

public class TrainingEnv {

    public static void main(String[] args) {
        TrainingEnv app = new TrainingEnv();
        GatewayServer server = new GatewayServer(app);
        server.start();
        System.out.println("Servidor de env de treinamento iniciado");
    }

    public int[][] ranges() { return SituacaoJogo.ranges; }

    public Episodio novoEpisodio() {
        return new Episodio();
    }

    private static class EstrategiaJogaPrimeiraCarta implements Estrategia {

        @Override
        public int joga(SituacaoJogo s) {
            return 0;
        }

        @Override
        public void partidaFinalizada(int numEquipeVencedora) {
        }

        @Override
        public boolean aceitaTruco(SituacaoJogo s) {
            return false;
        }

        @Override
        public boolean aceitaMaoDeX(Carta[] cartasParceiro, SituacaoJogo s) {
            return false;
        }
    }

    private static class Episodio {

        private EstrategiaAgente estrategia;
        private PartidaLocal partida;
        private int posicaoAgente;

        /**
         * Inicia uma nova partida com 4 bots (o primeiro controlado pelo agente)
         * e aguarda a vez do agente jogar
         */
        public Episodio() {
            partida = new PartidaLocal(false, false, "P");
//            partida.setSeedBaralho(123);
            partida.setPerformanceMaxima(true);
            estrategia = new EstrategiaAgente();
            posicaoAgente = 1;
            partida.adiciona(bot(estrategia));
            partida.adiciona(bot(new EstrategiaJogaPrimeiraCarta()));
            partida.adiciona(bot(new EstrategiaJogaPrimeiraCarta()));
            partida.adiciona(bot(new EstrategiaJogaPrimeiraCarta()));
            (new Thread(partida)).start();
            estrategia.aguardaVezDoAgente();
        }

        private JogadorBot bot(Estrategia e) {
            JogadorBot bot = new JogadorBot(e);
            bot.setFingeQuePensa(false);
            bot.setPerformanceMaxima(true);
            return bot;
        }

        /**
         * Executa uma ação no jogo e aguard a nova vez do agente jogar
         *
         * @param action índice da carta a jogar, ou -1 para pedir truco
         */
        public void executa(int action) {
            estrategia.executa(action);
        }

        /**
         * Recupera a tupla que representa o estado atual do jogo (observação)
         *
         * @return string contendo os valores separados por espaços; null
         *         se não for a vez do agente
         */
        public String estado() {
            return estrategia.situacaoJogo.toObservation();
        }

        /**
         * Encerra a partida em andamento, liberando as threads e o gc
         */
        public void finaliza() {
            if (partida != null) {
                if (!partida.finalizada) {
                    partida.abandona(posicaoAgente);
                }
            }
            estrategia.action = 0; // Libera a thread do agente
            partida = null;
        }
    }

    private static class EstrategiaAgente implements Estrategia {

        /**
         * Estado do jogo quando é a vez do agente jogar (null se não for a vez dele)
         */
        public SituacaoJogo situacaoJogo;

        /**
         * Ação a ser executada pelo agente (índice da carta a jogar, -1 para pedir
         * truco, null se estamos aguardando o agente)
         */
        private Integer action;
        private Thread threadAguardando;

        /**
         * Chamado pelo jogo quando é a vez do agente jogar. O método
         * disponibiliza o estado atual para o agente, aguarda a jogada dele
         * e a executa na partida.
         *
         * @param s
         *            Situação da partida no momento
         * @return
         */
        @Override
        public int joga(SituacaoJogo s) {
            action = null;
            situacaoJogo = s;
            aguardaAgenteJogar();
            return action;
        }

        /**
         * Chamado pelo jogo quando uma partida acaba. O método disponibiliza
         * o estado final para o agente, que deve encerrar o episódiio.
         *
         * @param numEquipeVencedora 1 ou 2, dependendo de qual equipe ganhou o jogo
         */
        @Override
        public void partidaFinalizada(int numEquipeVencedora) {
            situacaoJogo = new SituacaoJogo(numEquipeVencedora);
            action = 0;
        }

        /**
         * Chamado quando o env (Python/gym) quer executar uma ação
         *
         * @param a ação a ser executada
         */
        public void executa(int a) {
            action = a;
            situacaoJogo = null;
            aguardaVezDoAgente();
        }

        // TODO implementar esses outros eventos do jogo

        @Override
        public boolean aceitaTruco(SituacaoJogo s) {
            return false;
        }

        @Override
        public boolean aceitaMaoDeX(Carta[] cartasParceiro, SituacaoJogo s) {
            return true;
        }

        public void aguardaVezDoAgente() {
            aguarda(() -> { return situacaoJogo != null; });
        }

        public void aguardaAgenteJogar() {
            aguarda(() -> { return action != null; });
        }

        // TODO fazer um sleep longo e interrupt aqui ao invés de sleep(1)

        private void aguarda(Callable<Boolean> estado) {
            while (true) {
                try {
                    if (!!estado.call()) break;
                } catch (Exception e) {
                    // Um erro aqui significa que não tem mais o que aguardar
                    break;
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // Se for interrompida também não temos o que aguardar
                    break;
                }
            }
        }
    }
}
