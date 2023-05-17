package me.chester.minitruco.core;

/**
 * O modo "manilha velha" segue as mesmas regras do truco paulista,
 * mas com as manilhas fixas do truco mineiro
 */
public class ModoManilhaVelha extends ModoPaulista {
    @Override
    public boolean isManilhaVelha() {
        return true;
    }
}
