package me.chester.minitruco.android;

import android.app.Activity;

import me.chester.minitruco.android.multiplayer.ActivityMultiplayer;
import me.chester.minitruco.core.Partida;

/**
 * Cria uma nova partida, associando-a (e seu JogadorHumano) à activity
 * chamadora (servidor Bluetooth, cliente Bluetooth ou cliente Internet).
 */
public class CriadorDePartida {

    private static ActivityMultiplayer<Activity> activity;

    public static void setActivity(ActivityMultiplayer activityChamadora) {
        activity = activityChamadora;
    }

    public static Partida criaNovaPartida(JogadorHumano jogadorHumano) {
        if (activity == null || ((Activity)activity).isFinishing()) {
            return null;
        }
        return activity.criaNovaPartida(jogadorHumano);
    }

}
