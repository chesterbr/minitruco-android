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
     * Classes desse tipo devem implementar este método (que cria uma nova
     * partida e associa ao JogadorHumano
     */
    public Partida criaNovaPartida(JogadorHumano jogadorHumano);

}
