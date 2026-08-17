package me.chester.minitruco.android;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.JogadorBot;
import me.chester.minitruco.core.PartidaLocal;

class JogadorHumanoTest {

    JogadorHumano jogadorHumano;
    private MesaView mockMesaView;

    @BeforeEach
    void setUp() {
        mockMesaView = mock(MesaView.class);
        jogadorHumano = spy(new JogadorHumano(mock(TrucoActivity.class), mockMesaView));
    }

    @Test
    void dizFraseDeVitoriaOuDerrotaNoFimDoJogo() {
        doReturn(1).when(jogadorHumano).getEquipe();
        jogadorHumano.jogoFechado(1, 0);
        jogadorHumano.jogoFechado(2, 0);

        doReturn(2).when(jogadorHumano).getEquipe();
        jogadorHumano.jogoFechado(1, 0);
        jogadorHumano.jogoFechado(2, 0);

        InOrder inOrder = inOrder(mockMesaView);

        inOrder.verify(mockMesaView).diz(eq("vitoria"), eq(1), anyInt(), eq(0));
        inOrder.verify(mockMesaView).diz(eq("derrota"), eq(1), anyInt(), eq(0));
        inOrder.verify(mockMesaView).diz(eq("derrota"), eq(1), anyInt(), eq(0));
        inOrder.verify(mockMesaView).diz(eq("vitoria"), eq(1), anyInt(), eq(0));
    }

    @Test
    void indicaOPeDuranteADecisaoDeMaoDeX() {
        PartidaLocal partida = new PartidaLocal(false, false, "P");
        partida.adiciona(jogadorHumano);      // posição absoluta 1
        partida.adiciona(new JogadorBot());   // posição 2
        partida.adiciona(new JogadorBot());   // posição 3 - será o "mão"
        partida.adiciona(new JogadorBot());   // posição 4

        Carta[] cartasParceiro = new Carta[3];
        jogadorHumano.inicioMao(partida.getJogador(3));
        jogadorHumano.informaMaoDeX(cartasParceiro);

        // mão (abs 3) visto da posição 1 -> tela 3; pé (abs 2) -> tela 2
        verify(mockMesaView).setPosicaoVez(3);
        verify(mockMesaView).maoDeX(cartasParceiro, 2);
    }

    @Test
    void indicaOPeComWrapAroundQuandoMaoEstaNaPosicao1() {
        PartidaLocal partida = new PartidaLocal(false, false, "P");
        partida.adiciona(new JogadorBot());   // posição 1 - será o "mão"
        partida.adiciona(jogadorHumano);      // posição absoluta 2
        partida.adiciona(new JogadorBot());   // posição 3
        partida.adiciona(new JogadorBot());   // posição 4

        Carta[] cartasParceiro = new Carta[3];
        jogadorHumano.inicioMao(partida.getJogador(1));
        jogadorHumano.informaMaoDeX(cartasParceiro);

        // mão (abs 1) visto da posição 2 -> tela 4; pé (abs 4) -> tela 3
        verify(mockMesaView).setPosicaoVez(4);
        verify(mockMesaView).maoDeX(cartasParceiro, 3);
    }
}
