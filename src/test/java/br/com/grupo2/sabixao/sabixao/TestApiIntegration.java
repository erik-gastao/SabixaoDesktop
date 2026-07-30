package br.com.grupo2.sabixao.sabixao;

import br.com.grupo2.sabixao.sabixao.model.Pergunta;
import br.com.grupo2.sabixao.sabixao.service.ApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de integração real com a Open Trivia API.
 *
 * Era um main() em src/test: o surefire nunca o executava, então ninguém rodava.
 * Agora é JUnit, mas marcado com @Tag("integracao") e excluído do build normal
 * porque depende de internet e leva alguns segundos.
 *
 * Para rodar só ele:  mvn test -Dgroups=integracao
 */
@Tag("integracao")
class TestApiIntegration {

    @Test
    @DisplayName("API externa devolve perguntas utilizáveis")
    void buscaPerguntasReais() {
        try (ApiService apiService = new ApiService()) {
            List<Pergunta> perguntas = apiService.fetchTriviaQuestions(5, "medium", null);

            assertNotNull(perguntas, "fetchTriviaQuestions não deve devolver null");
            assertFalse(perguntas.isEmpty(),
                "nenhuma pergunta retornada - verifique a conexão com a internet");
            assertEquals(5, perguntas.size(), "foram pedidas 5 perguntas");

            for (Pergunta p : perguntas) {
                assertNotNull(p.getTexto());
                assertFalse(p.getTexto().isBlank(), "pergunta sem texto");
                assertTrue(p.getOpcoes().size() >= 2,
                    "pergunta precisa de pelo menos 2 alternativas");
                assertTrue(p.getRespostaCorreta() >= 0
                        && p.getRespostaCorreta() < p.getOpcoes().size(),
                    "índice da resposta correta fora da lista de opções");
                assertTrue(p.verificarResposta(p.getRespostaCorreta()),
                    "a alternativa apontada como correta deve ser aceita");
            }
        }
    }
}
