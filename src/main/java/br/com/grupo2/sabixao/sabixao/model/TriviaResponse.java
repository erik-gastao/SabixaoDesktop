package br.com.grupo2.sabixao.sabixao.model;

import java.util.List;

/**
 * Classe para deserializar a resposta da Open Trivia Database API
 * Exemplo de resposta: https://opentrivia.com/api.php?amount=10
 */
public class TriviaResponse {
    private int response_code; // 0 = sucesso, 1 = sem resultados, 2 = parâmetro inválido
    private List<TriviaQuestion> results;

    public TriviaResponse() {
    }

    public int getResponseCode() {
        return response_code;
    }

    public void setResponseCode(int response_code) {
        this.response_code = response_code;
    }

    public List<TriviaQuestion> getResults() {
        return results;
    }

    public void setResults(List<TriviaQuestion> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "TriviaResponse{" +
                "response_code=" + response_code +
                ", results=" + (results != null ? results.size() : 0) + " questions" +
                '}';
    }
}
