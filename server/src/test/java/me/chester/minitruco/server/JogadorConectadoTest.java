package me.chester.minitruco.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

class JogadorConectadoTest {

    private Socket mockSocket;
    private ByteArrayOutputStream outStream;

    @BeforeEach
    void setUp() throws IOException {
        mockSocket = mock(Socket.class);
        when(mockSocket.getInetAddress()).thenReturn(mock(InetAddress.class));
        when(mockSocket.getInetAddress().getHostAddress()).thenReturn("");
        when(mockSocket.getInputStream()).thenReturn(mock(InputStream.class));
        when(mockSocket.getOutputStream()).thenReturn(mock(OutputStream.class));
    }

    private JogadorConectado setupJogadorComInput(String input) throws IOException {
        ByteArrayInputStream inStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        outStream = new ByteArrayOutputStream();
        when(mockSocket.getInputStream()).thenReturn(inStream);
        when(mockSocket.getOutputStream()).thenReturn(outStream);
        return new JogadorConectado(mockSocket);
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

    @Test
    void testNovoJogadorTemNomePadrao() {
        JogadorConectado j = new JogadorConectado(mockSocket);
        assertThat(j.getNome(), matchesPattern("^sem_nome_\\d{1,3}$"));
    }

    @Test
    void testConectaViaHttp101() throws IOException {
        JogadorConectado j = setupJogadorComInput("GET /conectar HTTP/1.1\r\nHost: localhost\r\n\r\nN Alice\r\n");
        j.run();
        String output = outStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.startsWith("HTTP/1.1 101 Switching Protocols\r\nUpgrade: minitruco\r\nConnection: Upgrade\r\n\r\n"));
        assertEquals("Alice", j.getNome());
    }

    @Test
    void testStatusViaHttp200() throws IOException {
        JogadorConectado j = setupJogadorComInput("GET /status HTTP/1.1\r\nHost: localhost\r\n\r\n");
        j.run();
        String output = outStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("HTTP/1.1 200 OK"));
        assertTrue(output.contains("OK"));
    }

    @Test
    void testRequestDesconhecidoHttp404() throws IOException {
        JogadorConectado j = setupJogadorComInput("GET /desconhecido HTTP/1.1\r\nHost: localhost\r\n\r\n");
        j.run();
        String output = outStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("HTTP/1.1 404 NOT FOUND"));
    }

    @Test
    void testConexaoLegadaSemHttp() throws IOException {
        JogadorConectado j = setupJogadorComInput("N Bob\r\n");
        j.run();
        String output = outStream.toString(StandardCharsets.UTF_8);
        assertFalse(output.contains("HTTP/1.1"));
        assertEquals("Bob", j.getNome());
    }

    @Test
    void testKeepaliveNaoEnviadoAntesDoUpgradeOuStatus() throws Exception {
        java.io.PipedOutputStream pos = new java.io.PipedOutputStream();
        java.io.PipedInputStream pis = new java.io.PipedInputStream(pos);

        when(mockSocket.getInputStream()).thenReturn(pis);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(outStream);

        JogadorConectado j = new JogadorConectado(mockSocket);
        Thread t = Thread.ofVirtual().unstarted(j);
        t.start();

        // Simula cliente lento: espera 2.2 segundos antes de mandar a requisição HTTP
        Thread.sleep(2200);
        pos.write("GET /status HTTP/1.1\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        pos.flush();

        t.join(5000);

        String output = outStream.toString(StandardCharsets.UTF_8);
        assertFalse(output.contains("K "), "Keepalive não deve ser enviado antes de concluir a requisição HTTP");
        assertTrue(output.startsWith("HTTP/1.1 200 OK"));
    }

    @Test
    void testKeepaliveNaoEnviadoAntesDoUpgrade101() throws Exception {
        java.io.PipedOutputStream pos = new java.io.PipedOutputStream();
        java.io.PipedInputStream pis = new java.io.PipedInputStream(pos);

        when(mockSocket.getInputStream()).thenReturn(pis);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(outStream);

        JogadorConectado j = new JogadorConectado(mockSocket);
        Thread t = Thread.ofVirtual().unstarted(j);
        t.start();

        // Simula cliente lento: espera 2.2s antes de mandar GET /conectar
        Thread.sleep(2200);
        pos.write("GET /conectar HTTP/1.1\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        pos.flush();

        Thread.sleep(500);

        String output = outStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.startsWith("HTTP/1.1 101 Switching Protocols"), "Deveria começar com HTTP/1.1 101 e não com keepalive");

        t.interrupt();
        t.join(2000);
    }

}
