package me.chester.minitruco.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

class JogadorConectadoTest {

    private Socket mockSocket;

    @BeforeEach
    void setUp() throws IOException {
        mockSocket = mock(Socket.class);
        when(mockSocket.getInetAddress()).thenReturn(mock(InetAddress.class));
        when(mockSocket.getInetAddress().getHostAddress()).thenReturn("");
        when(mockSocket.getInputStream()).thenReturn(mock(InputStream.class));
        when(mockSocket.getOutputStream()).thenReturn(mock(OutputStream.class));
    }

    @Test
    void testOnFinish() throws InterruptedException {
        JogadorConectado j = new JogadorConectado(mockSocket);
        Thread t = Thread.ofVirtual().unstarted(j);
        final boolean[] chamouOnFinished = new boolean[1];
        chamouOnFinished[0] = false;
        j.setOnFinished((threadPassadaNoCallback) -> {
            chamouOnFinished[0] = true;
            assertEquals(threadPassadaNoCallback, t);
        });
        t.start();
        t.interrupt();
        t.join();
        assertTrue(chamouOnFinished[0]);
    }

}
