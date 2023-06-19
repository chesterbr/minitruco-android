package me.chester.minitruco.android;

import static android.util.DisplayMetrics.DENSITY_XHIGH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MesaViewTest {

    private MesaView mesaView;

    @BeforeEach
    void setUp() throws Exception {
        Context mockContext = mock(Context.class);
        Resources mockResources = mock(Resources.class);
        DisplayMetrics mockDisplayMetrics = mock(DisplayMetrics.class);
        when(mockContext.getResources()).thenReturn(mockResources);
        when(mockResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);
        when(mockResources.getIdentifier("balao_beatle", "array", "me.chester.minitruco")).thenReturn(1);
        when(mockResources.getStringArray(1)).thenReturn(new String[]{"john", "paul", "george", "ringo"});
        mockDisplayMetrics.density = DENSITY_XHIGH;
        mesaView = spy(new MesaView(mockContext));
        doReturn(mockResources).when(mesaView).getResources();
    }

    @Test
    void dizEscolheFraseDoBalaoBaseadaEmRndFrase() {
        mesaView.diz("beatle", 1, 1, 0);
        assertEquals("john", mesaView.fraseBalao);
        mesaView.diz("beatle", 1, 1, 1);
        assertEquals("paul", mesaView.fraseBalao);
        mesaView.diz("beatle", 1, 1, 2);
        assertEquals("george", mesaView.fraseBalao);
        mesaView.diz("beatle", 1, 1, 3);
        assertEquals("ringo", mesaView.fraseBalao);
        mesaView.diz("beatle", 1, 1, 4);
        assertEquals("john", mesaView.fraseBalao);
    }

    @Test
    void dizEscolheFraseDoBalaoBaseadaNoTipo() {
        mesaView.diz("beatle", 1, 1, 0);
        assertEquals("john", mesaView.fraseBalao);
        mesaView.diz("cor", 1, 1, 0);
        assertEquals("azul", mesaView.fraseBalao);
    }

    @Test
    void dizNaoRepeteFraseDeUmMesmoTipoDeBalao() {
        mesaView.diz("beatle", 1, 1, 0);
        assertEquals("john", mesaView.fraseBalao);
        mesaView.diz("beatle", 1, 1, 0);
        assertEquals("paul", mesaView.fraseBalao);
        mesaView.diz("beatle", 1, 1, 0);
        assertEquals("john", mesaView.fraseBalao);
    }

}
