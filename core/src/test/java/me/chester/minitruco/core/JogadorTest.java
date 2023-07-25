package me.chester.minitruco.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class JogadorTest {

    private static String sanitizaNome(String nome) {
        return Jogador.sanitizaNome(nome);
    }

    @Test
    void sanitizaNomePreservaAlfaNuméricosAcentuadosEUnderscore() {
        assertEquals("Carlos", sanitizaNome("Carlos"));
        assertEquals("Pixel_2_XL", sanitizaNome("Pixel 2 XL"));
        assertEquals("Kindle_Fire", sanitizaNome("Kindle Fire"));
        assertEquals("João_Da_Silva_123", sanitizaNome("João Da Silva_123"));
        assertEquals("éíóúãõ_ç_á_à_â_ê_ô_ü_ñ", sanitizaNome("éíóúãõ ç á à â ê ô ü ñ"));
        assertEquals("ÉÍÓÚÃÕ_Ç_Á_À_Â_Ê_Ô_Ü_Ñ", sanitizaNome("ÉÍÓÚÃÕ Ç Á À Â Ê Ô Ü Ñ"));
        assertEquals("Carlos_san", sanitizaNome("こんにちは, Carlos-san!"));
        assertEquals("Carlos", sanitizaNome("💩Carlos💩"));
        assertEquals("1_2_3_4", sanitizaNome("-1_2-3 4-"));
        assertEquals("sem_espaços", sanitizaNome("sem espaços"));
        assertEquals("sem_espaços", sanitizaNome("   sem    espaços    "));
        assertEquals("semtabs", sanitizaNome("sem\ttabs"));
        assertEquals("sempipes", sanitizaNome("sem|pipes"));
        assertEquals("semastericos", sanitizaNome("sem*astericos"));
        assertEquals("sem_quebras_de_linha", sanitizaNome("sem\nquebras\r\nde\rlinha\n\r!\n\n\n\n!\r\r\r\r!"));
        assertEquals("sem_repetir_fim", sanitizaNome("sem repetir: -----_____-----! fim."));
        assertEquals("odd_whitespace", sanitizaNome("odd whitespace:	              ​    　"));
        assertEquals("C0_control_chars", sanitizaNome("C0 control chars: \u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\u000B\u000C\u000E\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u001B\u001C\u001D\u001E\u001F"));
        assertEquals("C1_control_chars", sanitizaNome("C1 control chars: \u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u008B\u008C\u008E\u008F\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u009B\u009C\u009E\u009F"));
        assertEquals("alignment_and_direction", sanitizaNome("­؀؁؂؃؄؅؜۝܏᠎​‌‍‎‏‪‫‬‭‮⁠⁡⁢⁣⁤⁦alignment and direction: ⁧⁨⁩⁪⁫⁬⁭⁮⁯﻿￹￺￻𑂽𛲠𛲡𛲢𛲣𝅳𝅴𝅵𝅶𝅷𝅸𝅹𝅺󠀁󠀠󠀡󠀢󠀣󠀤󠀥󠀦󠀧󠀨󠀩󠀪󠀫󠀬󠀭󠀮󠀯󠀰󠀱󠀲󠀳󠀴󠀵󠀶󠀷󠀸󠀹󠀺󠀻󠀼󠀽󠀾󠀿󠁀󠁁󠁂󠁃󠁄󠁅󠁆󠁇󠁈󠁉󠁊󠁋󠁌󠁍󠁎󠁏󠁐󠁑󠁒󠁓󠁔󠁕󠁖󠁗󠁘󠁙󠁚󠁛󠁜󠁝󠁞󠁟󠁠󠁡󠁢󠁣󠁤󠁥󠁦󠁧󠁨󠁩󠁪󠁫󠁬󠁭󠁮󠁯󠁰󠁱󠁲󠁳󠁴󠁵󠁶󠁷󠁸󠁹󠁺󠁻󠁼󠁽󠁾󠁿"));
        assertEquals("crashy_on_iOS_Power_h_0", sanitizaNome("crashy on iOS: Powerلُلُصّبُلُلصّبُررً ॣ ॣh ॣ ॣ冗🏳0🌈️జ్ఞ‌ా\uDB40\uDC00 "));
    }

    void assertNomeDefault(String nome) {
        String regex = "^sem_nome_\\d{1,3}$";
        assertTrue(nome.matches(regex), nome + " não deu match em " + regex);
    }

    @Test
    void sanitizaNomeUsaDefaultSeNãoTiverCaracteresVálidos() {
        assertNomeDefault(sanitizaNome(null));
        assertNomeDefault(sanitizaNome(""));
        assertNomeDefault(sanitizaNome("_"));
        assertNomeDefault(sanitizaNome("-"));
        assertNomeDefault(sanitizaNome("___--__"));
        assertNomeDefault(sanitizaNome("-------"));
        assertNomeDefault(sanitizaNome("💩"));
        assertNomeDefault(sanitizaNome("誰かの名前を日本語で"));
    }

    @Test
    void sanitizaNomeUsaDefaultSeForUmNomeReservado() {
        assertNomeDefault(sanitizaNome("bot"));
    }

    @Test
    void sanitizaNomeÉIdempotente() {
        String[] nomes = new String[]{
            "nome",
            "nome_com_underscore",
            "nome-com-hífen",
            "nome com espaços",
            "nome com espaços e _ e - e 💩 e 123",
            null,
            "",
            "sem_nome_123"};
        for (String nome : nomes) {
            String sanitizado = sanitizaNome(nome);
            assertEquals(sanitizado, sanitizaNome(sanitizado));
        }
    }

    @Test
    void sanitizaNomeLimitaTamanhoEm25CaracteresValidos() {
        assertEquals("1234567890123456789012345",
            sanitizaNome("!!$$|1234567890123456789012345"));
        assertEquals("1234567890123456789012345",
            sanitizaNome("!!$$|1234567890123456789012345excesso"));
        // Não deixa underscore no final se truncar no espaço
        assertEquals("1234567890_234567890_234",
            sanitizaNome("1234567890 234567890 234 6"));
    }
}
