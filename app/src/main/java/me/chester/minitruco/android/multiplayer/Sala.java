package me.chester.minitruco.android.multiplayer;

import android.app.Activity;

import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.core.Partida;

/**
 * Qualquer uma das activities que representam a "sala" a partir da qual as
 * partidas de truco (locais ou remotas) serão criadas.
 * <p>
 * Em qualquer caso, a activity deve disponibilizar métodos para criar novas
 * partidas e, caso haja uma ou mais conexões remotas, enviar linhas de texto.
 * <p>
 * Nos jogos multiplayer, ela também encapsula a conexão com a parte remota
 * e exibe a "sala" com os nomes dos jogadores.
 */
public interface Sala<T extends Activity> {

    /**
     * Cria uma nova partida.
     * <p>
     * Normalmente é chamado pelo CriadorDePartida, que sabe qual Sala
     * acionar (e, portanto, qual o tipo de partida apropriado).
     *
     * @param jogadorHumano jogador humano que será associado à partida e
     *                      à activity (para intermediação de eventos)
     */
    public Partida criaNovaPartida(JogadorHumano jogadorHumano);

    /**
     * Envia uma linha de texto para a conexão remota.
     * <p>
     * Se houver mais de uma, envia para todas.
     *
     * @param linha texto a ser enviado.
     */
    public void enviaLinha(String linha);

    /**
     * Envia uma linha de texto para uma das conexões remotas.
     * <p>
     * Se houver apenas uma conexão, ignora o parâmetro slot.
     *
     * @param linha texto a ser enviado.
     * @param slot especifica qual das conexões remotas deve receber a mensagem
     */
    public void enviaLinha(int slot, String linha);

}
