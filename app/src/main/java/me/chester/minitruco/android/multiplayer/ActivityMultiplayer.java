package me.chester.minitruco.android.multiplayer;

import android.app.Activity;

import me.chester.minitruco.android.JogadorHumano;
import me.chester.minitruco.core.Partida;

/**
 * Qualquer activity que gerencie uma ou mais conexões remotas (Bluetooth ou
 * Internet).
 */
public interface ActivityMultiplayer<T extends Activity> {

    /**
     * Cria uma nova partida
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
    void enviaLinha(String linha);

    /**
     * Envia uma linha de texto para uma das conexões remotas.
     * <p>
     * Se houver apenas uma conexão, ignora o parâmetro slot.
     *
     * @param linha texto a ser enviado.
     * @param slot especifica qual das conexões remotas deve receber a mensagem
     */
    public default void enviaLinha(int slot, String linha) {
        enviaLinha(linha);
    }

}
