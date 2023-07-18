package me.chester.minitruco.server;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

class ComandoTest {

    JogadorConectado jogador = mock(JogadorConectado.class);

    @Test
    void testComandoParaLetraEncontraAClasseCerta() {
        assertInstanceOf(ComandoN.class , Comando.comandoParaLetra('N'));
        assertInstanceOf(ComandoC.class , Comando.comandoParaLetra('C'));
        assertNull(Comando.comandoParaLetra('Z'));
        assertNull(Comando.comandoParaLetra('!'));
    }

    // Não consigo fazer isso rolar; estou quase achando que ou eu
    // implemento toda a burocracia de um command pattern, ou consolido
    // essas classes todas em uma só, e faço um switch case dentro dela.
    //
    // Por ora eu vou focar em escrever os testes necessários em arquivos
    // separados que chamam Comando.executa.
//    void testExecutaDelegaEPassaParâmetrosParaSubClasseApropriada() {
//        ComandoN comandoN = spy(ComandoN.class);
//        try (MockedStatic<Comando> comando = mockStatic(Comando.class)) {
//            comando.when(() -> Comando.comandoParaLetra('N')).thenReturn(comandoN);
//            Comando.interpreta("N foo bar baz", jogador);
//            comando.verify(() -> Comando.comandoParaLetra('N'));
//            verify(comandoN).executa(new String[]{"N", "foo", "bar", "baz"}, jogador);
//        }
//    }

    @Test
    void testExecutaIgnoraNullsELinhasVazias() {
        Comando.interpreta(null, jogador);
        verifyNoInteractions(jogador);
        Comando.interpreta("", jogador);
        verifyNoInteractions(jogador);
        Comando.interpreta(" ", jogador);
        verifyNoInteractions(jogador);
    }

}
