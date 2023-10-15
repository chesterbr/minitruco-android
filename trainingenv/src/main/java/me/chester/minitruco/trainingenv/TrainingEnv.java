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
        // app is now the gateway.entry_point
        GatewayServer server = new GatewayServer(app);
        server.start();
        System.out.println("Servidor de env de treinamento iniciado");
    }

    public Episodio novoEpisodio() {
        return new Episodio();
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
            estrategia = new EstrategiaAgente();
            posicaoAgente = 1;
            partida.adiciona(new JogadorBot(estrategia));
            partida.adiciona(bot());
            partida.adiciona(bot());
            partida.adiciona(bot());
            (new Thread(partida)).start();
            estrategia.aguardaVezDoAgente();
        }

        private static JogadorBot bot() {
            JogadorBot bot = new JogadorBot();
            bot.setFingeQuePensa(false);
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
            return estrategia.situacaoJogo.toString();
        }

        /**
         * Encerra a partida em andamento, liberando as threads e o gc
         */
        public void finaliza() {
            partida.abandona(posicaoAgente);
            partida = null;
            estrategia = null;
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

        /**
         * Chamado pelo jogo quando é a vez do agente jogar
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
                    throw new RuntimeException(e);
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
