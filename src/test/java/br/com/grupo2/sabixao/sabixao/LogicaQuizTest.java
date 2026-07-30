package br.com.grupo2.sabixao.sabixao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regras de jogo que não dependem de tela nem de rede.
 */
class LogicaQuizTest {

    @ParameterizedTest(name = "PIN \"{0}\" é válido")
    @ValueSource(strings = {"1", "1234", "000", "99999999"})
    void pinSoDeNumerosEValido(String pin) {
        assertTrue(InicioController.pinValido(pin));
    }

    @ParameterizedTest(name = "PIN \"{0}\" é inválido")
    @ValueSource(strings = {"", " ", "12a", "abc", "12 34", "12.3", "-1", "١٢٣"})
    void pinComQualquerCoisaAlemDeDigitoEInvalido(String pin) {
        assertFalse(InicioController.pinValido(pin));
    }

    @Test
    @DisplayName("PIN nulo não estoura NullPointerException")
    void pinNuloEInvalido() {
        assertFalse(InicioController.pinValido(null));
    }

    @ParameterizedTest(name = "{0}s restantes valem {1} pontos")
    @CsvSource({
        "30, 160",
        "15, 130",
        "1, 102",
        "0, 100"
    })
    void pontuacaoSomaBonusPorSegundoRestante(int tempoRestante, int esperado) {
        assertEquals(esperado, QuizController.calcularPontos(tempoRestante));
    }

    @Test
    @DisplayName("tempo negativo não vira desconto de pontos")
    void tempoNegativoNaoTiraPontos() {
        // verificarResposta(-1) usa o mesmo caminho quando o tempo esgota;
        // um tempoRestante negativo por corrida de timer não deve punir.
        assertEquals(100, QuizController.calcularPontos(-5));
    }
}
