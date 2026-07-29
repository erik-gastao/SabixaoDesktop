package br.com.grupo2.sabixao.sabixao.service;

import br.com.grupo2.sabixao.sabixao.model.Pergunta;
import br.com.grupo2.sabixao.sabixao.model.TriviaQuestion;
import br.com.grupo2.sabixao.sabixao.model.TriviaResponse;
import br.com.grupo2.sabixao.sabixao.model.Usuario;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Serviço para comunicação com APIs externas
 * Integra com Open Trivia Database (https://opentdb.com)
 */
public class ApiService {
    
    private static final String BASE_URL = "http://localhost:8080/api"; // Backend próprio (futuro)
    private static final String TRIVIA_API_URL = "https://opentdb.com/api.php"; // API externa principal
    private static final String BACKUP_API_URL = "https://the-trivia-api.com/v2/questions"; // API alternativa
    private final HttpClient httpClient;
    private final Gson gson;
    private final TranslatorService translator;
    // Pool para traduzir em paralelo (8 por vez) — sequencial levava minutos para 10 perguntas.
    // Threads daemon: não seguram o JVM aberto ao fechar a janela.
    private final ExecutorService translationPool = Executors.newFixedThreadPool(8, r -> {
        Thread t = new Thread(r, "translator");
        t.setDaemon(true);
        return t;
    });

    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
        this.gson = new Gson();
        this.translator = new TranslatorService();
    }

    /**
     * Busca perguntas da API externa Open Trivia Database
     * @param amount Quantidade de perguntas (1-50)
     * @param difficulty Dificuldade: "easy", "medium", "hard" (opcional)
     * @param category ID da categoria (opcional)
     * @return Lista de perguntas convertidas para o modelo interno
     */
    public List<Pergunta> fetchTriviaQuestions(int amount, String difficulty, String category) {
        // Tentar API principal primeiro
        List<Pergunta> questions = tryFetchFromMainAPI(amount, difficulty, category);
        
        // Se falhou, tentar API alternativa
        if (questions.isEmpty()) {
            System.out.println("⚠️ API principal falhou. Tentando API alternativa...");
            questions = tryFetchFromBackupAPI(amount, difficulty);
        }
        
        // As perguntas já vêm traduzidas de convertTriviaToQuestions.
        // Termos sem tradução (nomes próprios, siglas) permanecem em inglês.
        return questions;
    }
    
    /**
     * Tenta buscar da API principal (Open Trivia)
     */
    private List<Pergunta> tryFetchFromMainAPI(int amount, String difficulty, String category) {
        try {
            // Construir URL com parâmetros
            StringBuilder url = new StringBuilder(TRIVIA_API_URL);
            url.append("?amount=").append(Math.min(amount, 50)); // Máximo 50
            
            if (difficulty != null && !difficulty.isEmpty()) {
                url.append("&difficulty=").append(difficulty);
            }
            
            if (category != null && !category.isEmpty()) {
                url.append("&category=").append(category);
            }
            
            url.append("&type=multiple"); // Apenas perguntas de múltipla escolha

            System.out.println("📡 Tentando API principal: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .header("Accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(10))
                .GET()
                .build();

            System.out.println("⏳ Aguardando resposta da API...");

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            System.out.println("📥 Resposta recebida - Status: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("✅ Status 200 OK - Parseando JSON...");
                TriviaResponse triviaResponse = gson.fromJson(response.body(), TriviaResponse.class);
                
                if (triviaResponse.getResponseCode() == 0 && triviaResponse.getResults() != null) {
                    System.out.println("✅ JSON válido - " + triviaResponse.getResults().size() + " perguntas encontradas");
                    return convertTriviaToQuestions(triviaResponse.getResults());
                } else {
                    System.err.println("❌ API retornou código de erro: " + triviaResponse.getResponseCode());
                }
            } else {
                System.err.println("❌ Erro HTTP: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Exceção na API principal: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Tenta buscar da API alternativa (The Trivia API)
     */
    private List<Pergunta> tryFetchFromBackupAPI(int amount, String difficulty) {
        try {
            StringBuilder url = new StringBuilder(BACKUP_API_URL);
            url.append("?limit=").append(Math.min(amount, 50));
            
            if (difficulty != null && !difficulty.isEmpty()) {
                // The Trivia API usa diferentes valores: easy, medium, hard
                url.append("&difficulty=").append(difficulty);
            }

            System.out.println("📡 Tentando API alternativa: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .header("Accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(10))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            System.out.println("📥 API alternativa - Status: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("✅ Usando API alternativa!");
                return parseBackupAPIResponse(response.body());
            }
        } catch (Exception e) {
            System.err.println("❌ API alternativa também falhou: " + e.getMessage());
        }
        
        System.out.println("⚠️ Todas as APIs falharam - retornando lista vazia");
        return new ArrayList<>();
    }
    
    /**
     * Parseia resposta da API alternativa
     */
    private List<Pergunta> parseBackupAPIResponse(String jsonResponse) {
        try {
            // A API alternativa retorna array direto — converter para TriviaQuestion
            // e reutilizar o mesmo pipeline de tradução/embaralhamento da API principal
            com.google.gson.JsonArray jsonArray = gson.fromJson(jsonResponse, com.google.gson.JsonArray.class);
            List<TriviaQuestion> triviaQuestions = new ArrayList<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                com.google.gson.JsonObject obj = jsonArray.get(i).getAsJsonObject();

                TriviaQuestion tq = new TriviaQuestion();
                tq.setQuestion(obj.getAsJsonObject("question").get("text").getAsString());
                tq.setCorrectAnswer(obj.get("correctAnswer").getAsString());
                tq.setCategory(obj.get("category").getAsString());
                tq.setDifficulty(obj.get("difficulty").getAsString());

                List<String> incorretas = new ArrayList<>();
                com.google.gson.JsonArray incorrectAnswers = obj.getAsJsonArray("incorrectAnswers");
                for (int j = 0; j < incorrectAnswers.size() && j < 3; j++) {
                    incorretas.add(incorrectAnswers.get(j).getAsString());
                }
                tq.setIncorrectAnswers(incorretas);

                triviaQuestions.add(tq);
            }

            return convertTriviaToQuestions(triviaQuestions);
        } catch (Exception e) {
            System.err.println("❌ Erro ao parsear API alternativa: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Converte perguntas da API externa para o modelo interno,
     * traduzindo tudo em paralelo no pool
     */
    private List<Pergunta> convertTriviaToQuestions(List<TriviaQuestion> triviaQuestions) {
        System.out.println("🌍 Traduzindo perguntas para português (em paralelo)...");

        // 1ª passada: disparar TODAS as traduções de uma vez no pool (8 simultâneas)
        List<Future<String>> textFutures = new ArrayList<>();
        List<Future<String>> correctFutures = new ArrayList<>();
        List<List<Future<String>>> incorrectFutures = new ArrayList<>();

        for (TriviaQuestion tq : triviaQuestions) {
            textFutures.add(translateAsync(decodeHtml(tq.getQuestion())));
            correctFutures.add(translateAsync(decodeHtml(tq.getCorrectAnswer())));

            List<Future<String>> inc = new ArrayList<>();
            for (String incorrect : tq.getIncorrectAnswers()) {
                inc.add(translateAsync(decodeHtml(incorrect)));
            }
            incorrectFutures.add(inc);
        }

        // 2ª passada: montar as perguntas com os resultados
        List<Pergunta> questions = new ArrayList<>();
        for (int i = 0; i < triviaQuestions.size(); i++) {
            TriviaQuestion tq = triviaQuestions.get(i);
            Pergunta q = new Pergunta();

            q.setTexto(getTranslation(textFutures.get(i), decodeHtml(tq.getQuestion())));
            q.setCategoria(tq.getCategory());
            q.setDificuldade(tq.getDifficulty().toUpperCase());

            // Guardar a referência da tradução da correta — re-traduzir não é
            // determinístico e quebrava o indexOf após o shuffle
            String correctTranslated = getTranslation(correctFutures.get(i), decodeHtml(tq.getCorrectAnswer()));
            List<String> options = new ArrayList<>();
            options.add(correctTranslated);

            List<Future<String>> inc = incorrectFutures.get(i);
            List<String> originalIncorrect = tq.getIncorrectAnswers();
            for (int j = 0; j < inc.size(); j++) {
                options.add(getTranslation(inc.get(j), decodeHtml(originalIncorrect.get(j))));
            }

            // Embaralhar e localizar a correta pela referência já traduzida
            Collections.shuffle(options);
            q.setOpcoes(options);
            q.setRespostaCorreta(options.indexOf(correctTranslated));

            questions.add(q);
        }

        System.out.println("✅ Tradução concluída!");
        return questions;
    }

    /**
     * Dispara uma tradução no pool paralelo
     */
    private Future<String> translateAsync(String texto) {
        return translationPool.submit(() -> translator.translateToPortuguese(texto));
    }

    /**
     * Espera o resultado de uma tradução; em caso de erro devolve o texto original
     */
    private String getTranslation(Future<String> futuro, String fallback) {
        try {
            return futuro.get();
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Decodifica HTML entities (a API retorna texto codificado)
     */
    private String decodeHtml(String text) {
        if (text == null) return "";

        // NÃO usar URLDecoder aqui: o texto da API não é URL-encoded.
        // URLDecoder quebra com '%' literal (IllegalArgumentException) e troca '+' por espaço.

        // Substituir entidades HTML comuns
        return text
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&apos;", "'")
            .replace("&deg;", "°")
            .replace("&eacute;", "é")
            .replace("&rsquo;", "'");
    }

    /**
     * Registra um novo usuário no sistema
     * @param user O usuário a ser registrado
     * @return true se o registro foi bem-sucedido
     */
    public boolean registerUser(Usuario user) {
        try {
            // gson.toJson escapa aspas e caracteres especiais — String.format quebrava o payload
            String jsonBody = gson.toJson(user);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200 || response.statusCode() == 201;
        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao registrar usuário: " + e.getMessage());
            return false;
        }
    }

    /**
     * Autentica um usuário no sistema
     * @param nome Nome do usuário
     * @param senha Senha do usuário
     * @return O usuário se autenticado com sucesso, null caso contrário
     */
    public Usuario loginUser(String nome, String senha) {
        try {
            // gson.toJson escapa aspas e caracteres especiais — String.format quebrava o payload
            String jsonBody = gson.toJson(new Usuario(nome, senha));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // TODO: Parse JSON response para criar objeto Usuario
                return new Usuario(nome, senha);
            }
            return null;
        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao fazer login: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica se um PIN é válido
     * @param pin O PIN a ser verificado
     * @return true se o PIN é válido
     */
    public boolean validatePin(String pin) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/validate-pin?pin="
                    + java.net.URLEncoder.encode(pin, java.nio.charset.StandardCharsets.UTF_8)))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao validar PIN: " + e.getMessage());
            return false;
        }
    }
}
