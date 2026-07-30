package br.com.grupo2.sabixao.sabixao.service;

import br.com.grupo2.sabixao.sabixao.model.Pergunta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Conversão e limpeza de dados da API — sem tocar na rede.
 */
class ApiServiceTest {

    private static final String CORRETA = "Alexey Pajitnov";
    private static final List<String> INCORRETAS =
        List.of("Toru Iwatani", "Allan Alcorn", "Shigeru Miyamoto");

    /**
     * O embaralhamento é o ponto mais frágil do fluxo: já quebrou uma vez
     * re-traduzindo a alternativa correta depois do shuffle, o que fazia o
     * indexOf devolver -1 e nenhuma resposta ser aceita. Repetido porque o
     * shuffle é aleatório: uma execução só não prova nada.
     */
    @RepeatedTest(50)
    @DisplayName("respostaCorreta sempre aponta para a alternativa certa após o shuffle")
    void embaralhamentoPreservaARespostaCorreta() {
        Pergunta p = ApiService.buildQuestion(
            "Por quem o Tetris foi criado?", "Games", "medium", CORRETA, INCORRETAS);

        assertEquals(4, p.getOpcoes().size(), "deve ter 1 correta + 3 incorretas");
        assertTrue(p.getRespostaCorreta() >= 0, "índice da correta não pode ser -1");
        assertEquals(CORRETA, p.getOpcoes().get(p.getRespostaCorreta()));
        assertTrue(p.verificarResposta(p.getRespostaCorreta()));
    }

    @Test
    @DisplayName("dificuldade vai para maiúsculas e nula não estoura")
    void dificuldadeNormalizada() {
        Pergunta comDificuldade =
            ApiService.buildQuestion("t", "c", "medium", CORRETA, INCORRETAS);
        assertEquals("MEDIUM", comDificuldade.getDificuldade());

        Pergunta semDificuldade =
            ApiService.buildQuestion("t", "c", null, CORRETA, INCORRETAS);
        assertNull(semDificuldade.getDificuldade());
    }

    @Test
    @DisplayName("pergunta com 2 alternativas (tipo boolean) não perde a correta")
    void perguntaBooleanFunciona() {
        Pergunta p = ApiService.buildQuestion("t", "c", "easy", "True", List.of("False"));

        assertEquals(2, p.getOpcoes().size());
        assertEquals("True", p.getOpcoes().get(p.getRespostaCorreta()));
    }

    @ParameterizedTest(name = "decodeHtml(\"{0}\") -> \"{1}\"")
    @CsvSource({
        "'&quot;Jurassic Park&quot;', '\"Jurassic Park\"'",
        // apóstrofo dentro de valor entre aspas simples do CsvSource se escreve dobrado
        "'it&#039;s', 'it''s'",
        "'Tom &amp; Jerry', 'Tom & Jerry'",
        "'5 &lt; 10', '5 < 10'",
        "'10 &gt; 5', '10 > 5'",
        "'20&deg;C', '20°C'",
        "'ma&ntilde;ana', 'mañana'",
        "'caf&eacute;', 'café'"
    })
    void decodificaEntidadesNomeadas(String entrada, String esperado) {
        assertEquals(esperado, ApiService.decodeHtml(entrada));
    }

    @ParameterizedTest(name = "decodeHtml(\"{0}\") -> \"{1}\"")
    @CsvSource({
        "'don&#8217;t', 'don’t'",
        "'&#8230;', '…'",
        "'&#x22;a&#x22;', '\"a\"'",
        "'&#65;&#66;&#67;', 'ABC'"
    })
    @DisplayName("entidades numéricas (decimais e hex) também são decodificadas")
    void decodificaEntidadesNumericas(String entrada, String esperado) {
        assertEquals(esperado, ApiService.decodeHtml(entrada));
    }

    @Test
    @DisplayName("&amp; é resolvido por último para não estragar entidade dupla")
    void ordemDoAmpersand() {
        // Se &amp; fosse trocado primeiro, "&amp;lt;" viraria "&lt;" e depois "<".
        assertEquals("&lt;", ApiService.decodeHtml("&amp;lt;"));
    }

    @Test
    @DisplayName("texto nulo devolve string vazia em vez de estourar")
    void textoNulo() {
        assertEquals("", ApiService.decodeHtml(null));
    }

    @Test
    @DisplayName("texto com % literal passa intacto (URLDecoder quebrava aqui)")
    void percentualLiteralNaoQuebra() {
        assertEquals("100% cotton", ApiService.decodeHtml("100% cotton"));
    }
}
