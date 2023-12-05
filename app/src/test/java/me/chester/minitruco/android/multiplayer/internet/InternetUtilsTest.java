package me.chester.minitruco.android.multiplayer.internet;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import me.chester.minitruco.android.PreferenceUtils;

class InternetUtilsTest {

    private MockedStatic<PreferenceUtils> mockPreferenceUtils;
    private MockedConstruction<URL> urlMockedConstruction;

    @AfterEach
    public void afterEach() {
        if (mockPreferenceUtils != null) {
            mockPreferenceUtils.close();
        }
        if (urlMockedConstruction != null) {
            urlMockedConstruction.close();
        }
    }

    void mockModo(String value, String... values) {
        if (mockPreferenceUtils != null) {
            mockPreferenceUtils.close();
        }
        mockPreferenceUtils = mockStatic(PreferenceUtils.class);
        mockPreferenceUtils.when(() -> PreferenceUtils.getLetraDoModo(any())).thenReturn(value, values);
    }

    void mockAguardando(String modo) {
        if (urlMockedConstruction != null) {
            urlMockedConstruction.close();
        }
        urlMockedConstruction = mockConstruction(URL.class, (theMock, context) -> {
            HttpURLConnection mockHttpURLConnection = mock(HttpURLConnection.class);
            when(theMock.openConnection()).thenReturn((URLConnection) mockHttpURLConnection);
            when(mockHttpURLConnection.getInputStream()).thenReturn(new ByteArrayInputStream((
                "OK\n" +
                    "ONLINE 123\n" +
                    (modo != null ? "AGUARDANDO " + modo + "\n" : "") +
                    "K 12342342342\n"
            ).getBytes()));
        });
    }

    @Test
    void testPromoveJogoInternetSoETrueSeTemSalaAguardandoNoModoAtual() {
        mockAguardando("M");
        mockModo("M", "P", "L");
        assertTrue(InternetUtils.isPromoveJogoInternet(null, true));
        assertFalse(InternetUtils.isPromoveJogoInternet(null, true));
        assertFalse(InternetUtils.isPromoveJogoInternet(null, true));

        mockAguardando("ML");
        mockModo("M", "P", "L");
        assertTrue(InternetUtils.isPromoveJogoInternet(null, true));
        assertFalse(InternetUtils.isPromoveJogoInternet(null, true));
        assertTrue(InternetUtils.isPromoveJogoInternet(null, true));

        mockAguardando("");
        mockModo("M", "P", "L");
        assertFalse(InternetUtils.isPromoveJogoInternet(null, true));
        assertFalse(InternetUtils.isPromoveJogoInternet(null, true));
        assertFalse(InternetUtils.isPromoveJogoInternet(null, true));
    }

    @Test
    void testPromoveJogoInternetIgnoraSeNaoTiverInfoDeAguardandoNoStatus() {
        mockAguardando(null);
        mockModo("M", "P", "L");
        assertFalse(InternetUtils.isPromoveJogoInternet(null, true));
        assertFalse(InternetUtils.isPromoveJogoInternet(null, true));
        assertFalse(InternetUtils.isPromoveJogoInternet(null, true));
    }

    @Test
    void testPromoveJogoInternetNaoPromoveDuasVezesSeNaoPedirRepeticao() {
        mockAguardando("M");
        mockModo("M", "M", "M");
        assertTrue(InternetUtils.isPromoveJogoInternet(null, false));
        assertFalse(InternetUtils.isPromoveJogoInternet(null, false));
        assertFalse(InternetUtils.isPromoveJogoInternet(null, false));
    }
}
