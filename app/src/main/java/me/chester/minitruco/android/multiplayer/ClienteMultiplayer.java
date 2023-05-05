package me.chester.minitruco.android.multiplayer;

public interface ClienteMultiplayer {
    // TODO rever esse getter introduzido para poder generalizar
    //     JogoRemoto entre internet e bluetooth que é protected
    //     e está na classe base
    String getRegras();

    void enviaLinha(String linha);
}
