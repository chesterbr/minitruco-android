package me.chester.minitruco.android;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

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
}
