package me.chester.minitruco.android;

import me.chester.minitruco.core.Partida;

/**
 * Centraliza a criação de novas partidas, guardando uma referência à
 * Sala para qual a criação (e a comunicação remota, se houver)
 * será delegada.
 */
public class CriadorDePartida {

    private static SalaActivity sala;

    /**
     * Prepara o CriadorDePartida para criar o tipo de partida apropriado
     * para o modo correspondente à activity chamadora (guardando a instância
     * para associar às partidas).
     * <p>
     * É recomendável setar no onCreate() e no onResume() da activity chaamdora,
     * para garantir que a instância atual seja sempre a correta.
     *
     * @param sala em modo multiplayer, a activity que exibe a sala;
     *                          null no single player.
     */
    public static void setActivitySala(SalaActivity sala) {
        CriadorDePartida.sala = sala;
    }

    public static Partida criaNovaPartida(JogadorHumano jogadorHumano) {
        if (sala == null || sala.isFinishing()) {
            return null;
        }
        return sala.criaNovaPartida(jogadorHumano);
    }

}
