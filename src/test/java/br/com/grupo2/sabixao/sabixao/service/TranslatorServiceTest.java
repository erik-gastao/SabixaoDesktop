package br.com.grupo2.sabixao.sabixao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Limpeza e validação da resposta do tradutor — sem tocar na rede.
 */
class TranslatorServiceTest {

    @ParameterizedTest(name = "limpar(\"{0}\") -> \"{1}\"")
    @CsvSource({
        "'- Qual é a montanha mais alta do mundo?', 'Qual é a montanha mais alta do mundo?'",
        "'– Quem descobriu o Brasil?', 'Quem descobriu o Brasil?'",
        "'— Onde fica Lisboa?', 'Onde fica Lisboa?'",
        "'  -  Com espaços  ', 'Com espaços'",
        "'Sem prefixo nenhum', 'Sem prefixo nenhum'"
    })
    @DisplayName("travessão que o MyMemory adiciona no começo é removido")
    void removePrefixoDeTravessao(String entrada, String esperado) {
        assertEquals(esperado, TranslatorService.clean(entrada));
    }

    @Test
    @DisplayName("hífen no meio do texto é preservado")
    void naoMexeEmHifenInterno() {
        assertEquals("Bem-vindo ao quiz", TranslatorService.clean("Bem-vindo ao quiz"));
        assertEquals("guarda-chuva - azul", TranslatorService.clean("- guarda-chuva - azul"));
    }

    @Test
    void nuloContinuaNulo() {
        assertNull(TranslatorService.clean(null));
    }

    @Test
    @DisplayName("tradução igual ao original é descartada")
    void traducaoIgualNaoServe() {
        assertFalse(TranslatorService.isUsableTranslation("Hello", "Hello"));
    }

    @Test
    @DisplayName("aviso de cota do MyMemory é descartado")
    void avisoDeCotaNaoServe() {
        assertFalse(TranslatorService.isUsableTranslation(
            "MYMEMORY WARNING: YOU USED ALL AVAILABLE FREE TRANSLATIONS", "Hello"));
    }

    @Test
    void vazioOuNuloNaoServe() {
        assertFalse(TranslatorService.isUsableTranslation("", "Hello"));
        assertFalse(TranslatorService.isUsableTranslation(null, "Hello"));
    }

    @Test
    void traducaoValidaPassa() {
        assertTrue(TranslatorService.isUsableTranslation("Olá", "Hello"));
    }
}
