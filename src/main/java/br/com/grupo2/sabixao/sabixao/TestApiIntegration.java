package br.com.grupo2.sabixao.sabixao;

import br.com.grupo2.sabixao.sabixao.model.Question;
import br.com.grupo2.sabixao.sabixao.service.ApiService;

import java.util.List;

/**
 * Classe de teste para validar integração com Open Trivia API
 * Execute este main para testar a chamada da API
 */
public class TestApiIntegration {
    
    public static void main(String[] args) {
        System.out.println("=== TESTANDO INTEGRAÇÃO COM OPEN TRIVIA API ===\n");
        
        ApiService apiService = new ApiService();
        
        System.out.println("Buscando 5 perguntas de dificuldade média...\n");
        
        List<Question> questions = apiService.fetchTriviaQuestions(5, "medium", null);
        
        if (questions.isEmpty()) {
            System.out.println("❌ ERRO: Nenhuma pergunta foi retornada!");
            System.out.println("Verifique sua conexão com a internet.");
            return;
        }
        
        System.out.println("✅ SUCESSO! " + questions.size() + " perguntas carregadas da API:\n");
        
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println("--- PERGUNTA " + (i + 1) + " ---");
            System.out.println("Categoria: " + q.getCategoria());
            System.out.println("Dificuldade: " + q.getDificuldade());
            System.out.println("Pergunta: " + q.getTexto());
            System.out.println("Opções:");
            for (int j = 0; j < q.getOpcoes().size(); j++) {
                String marcador = (j == q.getRespostaCorreta()) ? " ✓ (CORRETA)" : "";
                System.out.println("  " + (char)('A' + j) + ") " + q.getOpcoes().get(j) + marcador);
            }
            System.out.println();
        }
        
        System.out.println("=== TESTE CONCLUÍDO COM SUCESSO ===");
    }
}
