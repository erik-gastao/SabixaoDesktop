package br.com.grupo2.sabixao.sabixao.service;

import br.com.grupo2.sabixao.sabixao.model.Pergunta;
import br.com.grupo2.sabixao.sabixao.model.TriviaQuestion;
import br.com.grupo2.sabixao.sabixao.model.TriviaResponse;
import br.com.grupo2.sabixao.sabixao.model.Usuario;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serviço para comunicação com APIs externas
 * Integra com Open Trivia Database (https://opentdb.com)
 *
 * Fecha com {@link #close()}: cada instância abre um pool de tradução próprio.
 *
 * Atenção: os métodos de usuário ({@link #registerUser}, {@link #loginUser},
 * {@link #validatePin}) falam com um backend em localhost:8080 que ainda não
 * existe e por isso nenhuma tela os chama. Enquanto não houver servidor, o
 * cadastro roda em {@link SessaoLocal}.
 */
public class ApiService implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(ApiService.class.getName());

    private static final String BASE_URL = "http://localhost:8080/api"; // Backend próprio (futuro)
    private static final String TRIVIA_API_URL = "https://opentdb.com/api.php"; // API externa principal
    private static final String BACKUP_API_URL = "https://the-trivia-api.com/v2/questions"; // API alternativa
    private static final int MAX_PERGUNTAS_POR_CHAMADA = 50;
    private static final int TAMANHO_POOL_TRADUCAO = 8;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final Gson gson;
    private final TranslatorService translator;
    // Pool para traduzir em paralelo — sequencial levava minutos para 10 perguntas.
    // Threads daemon: não seguram o JVM aberto ao fechar a janela.
    private final ExecutorService translationPool =
        Executors.newFixedThreadPool(TAMANHO_POOL_TRADUCAO, r -> {
            Thread t = new Thread(r, "translator");
            t.setDaemon(true);
            return t;
        });

    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
        this.gson = new Gson();
        this.translator = new TranslatorService();
    }

    /**
     * Encerra o pool de tradução. Sem isso cada ApiService criado deixa
     * {@value #TAMANHO_POOL_TRADUCAO} threads vivas até o app fechar.
     */
    @Override
    public void close() {
        translationPool.shutdownNow();
    }

    /**
     * Busca perguntas da API externa Open Trivia Database
     * @param amount Quantidade de perguntas (1-50)
     * @param difficulty Dificuldade: "easy", "medium", "hard" (opcional)
     * @param category ID da categoria (opcional)
     * @return Lista de perguntas convertidas para o modelo interno
     */
    public List<Pergunta> fetchTriviaQuestions(int amount, String difficulty, String category) {
        List<Pergunta> questions = tryFetchFromMainAPI(amount, difficulty, category);

        if (questions.isEmpty()) {
            LOG.warning("API principal falhou - tentando API alternativa");
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
        StringBuilder url = new StringBuilder(TRIVIA_API_URL);
        url.append("?amount=").append(Math.min(amount, MAX_PERGUNTAS_POR_CHAMADA));

        if (difficulty != null && !difficulty.isEmpty()) {
            url.append("&difficulty=").append(difficulty);
        }
        if (category != null && !category.isEmpty()) {
            url.append("&category=").append(category);
        }
        url.append("&type=multiple"); // Apenas perguntas de múltipla escolha

        LOG.log(Level.INFO, "Consultando API principal: {0}", url);

        try {
            HttpResponse<String> response = enviarGet(url.toString());

            if (response.statusCode() != 200) {
                LOG.log(Level.WARNING, "API principal respondeu HTTP {0}", response.statusCode());
                return new ArrayList<>();
            }

            TriviaResponse triviaResponse = gson.fromJson(response.body(), TriviaResponse.class);
            if (triviaResponse.getResponseCode() != 0 || triviaResponse.getResults() == null) {
                LOG.log(Level.WARNING, "API principal retornou responseCode {0}",
                    triviaResponse.getResponseCode());
                return new ArrayList<>();
            }

            LOG.log(Level.INFO, "{0} perguntas recebidas da API principal",
                triviaResponse.getResults().size());
            return convertTriviaToQuestions(triviaResponse.getResults());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // não engolir o pedido de cancelamento
            LOG.log(Level.WARNING, "Consulta à API principal interrompida", e);
        } catch (IOException | JsonSyntaxException e) {
            LOG.log(Level.WARNING, "Falha na API principal", e);
        }

        return new ArrayList<>();
    }

    /**
     * Tenta buscar da API alternativa (The Trivia API)
     */
    private List<Pergunta> tryFetchFromBackupAPI(int amount, String difficulty) {
        StringBuilder url = new StringBuilder(BACKUP_API_URL);
        url.append("?limit=").append(Math.min(amount, MAX_PERGUNTAS_POR_CHAMADA));

        if (difficulty != null && !difficulty.isEmpty()) {
            // The Trivia API usa os mesmos valores: easy, medium, hard
            url.append("&difficulty=").append(difficulty);
        }

        LOG.log(Level.INFO, "Consultando API alternativa: {0}", url);

        try {
            HttpResponse<String> response = enviarGet(url.toString());

            if (response.statusCode() == 200) {
                return parseBackupAPIResponse(response.body());
            }
            LOG.log(Level.WARNING, "API alternativa respondeu HTTP {0}", response.statusCode());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "Consulta à API alternativa interrompida", e);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Falha na API alternativa", e);
        }

        LOG.warning("Todas as APIs falharam - retornando lista vazia");
        return new ArrayList<>();
    }

    private HttpResponse<String> enviarGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .timeout(TIMEOUT)
            .GET()
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Parseia resposta da API alternativa
     */
    private List<Pergunta> parseBackupAPIResponse(String jsonResponse) {
        try {
            // A API alternativa retorna array direto — converter para TriviaQuestion
            // e reutilizar o mesmo pipeline de tradução/embaralhamento da API principal
            JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);
            List<TriviaQuestion> triviaQuestions = new ArrayList<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject obj = jsonArray.get(i).getAsJsonObject();

                TriviaQuestion tq = new TriviaQuestion();
                tq.setQuestion(obj.getAsJsonObject("question").get("text").getAsString());
                tq.setCorrectAnswer(obj.get("correctAnswer").getAsString());
                tq.setCategory(obj.get("category").getAsString());
                tq.setDifficulty(obj.get("difficulty").getAsString());

                List<String> incorretas = new ArrayList<>();
                JsonArray incorrectAnswers = obj.getAsJsonArray("incorrectAnswers");
                for (int j = 0; j < incorrectAnswers.size() && j < 3; j++) {
                    incorretas.add(incorrectAnswers.get(j).getAsString());
                }
                tq.setIncorrectAnswers(incorretas);

                triviaQuestions.add(tq);
            }

            return convertTriviaToQuestions(triviaQuestions);
        } catch (JsonSyntaxException | IllegalStateException | NullPointerException e) {
            LOG.log(Level.WARNING, "JSON inesperado na API alternativa", e);
            return new ArrayList<>();
        }
    }

    /**
     * Converte perguntas da API externa para o modelo interno,
     * traduzindo tudo em paralelo no pool
     */
    private List<Pergunta> convertTriviaToQuestions(List<TriviaQuestion> triviaQuestions) {
        LOG.info("Traduzindo perguntas para português (em paralelo)");

        // 1ª passada: disparar TODAS as traduções de uma vez no pool
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

            String texto = getTranslation(textFutures.get(i), decodeHtml(tq.getQuestion()));
            String correta = getTranslation(correctFutures.get(i), decodeHtml(tq.getCorrectAnswer()));

            List<Future<String>> inc = incorrectFutures.get(i);
            List<String> originalIncorrect = tq.getIncorrectAnswers();
            List<String> incorretas = new ArrayList<>();
            for (int j = 0; j < inc.size(); j++) {
                incorretas.add(getTranslation(inc.get(j), decodeHtml(originalIncorrect.get(j))));
            }

            questions.add(buildQuestion(texto, tq.getCategory(), tq.getDifficulty(), correta, incorretas));
        }

        LOG.info("Tradução concluída");
        return questions;
    }

    /**
     * Embaralha as alternativas e registra em qual índice a correta caiu.
     *
     * Estático e sem rede de propósito: é a parte mais frágil do fluxo (já
     * quebrou uma vez re-traduzindo a correta depois do shuffle, o que fazia o
     * indexOf devolver -1) e precisa de teste unitário.
     *
     * @param correta texto da alternativa correta, já traduzido
     * @param incorretas alternativas erradas, já traduzidas
     */
    static Pergunta buildQuestion(String texto, String categoria, String dificuldade,
                                  String correta, List<String> incorretas) {
        Pergunta pergunta = new Pergunta();
        pergunta.setTexto(texto);
        pergunta.setCategoria(categoria);
        pergunta.setDificuldade(dificuldade == null ? null : dificuldade.toUpperCase());

        List<String> opcoes = new ArrayList<>();
        opcoes.add(correta);
        opcoes.addAll(incorretas);

        // Localiza a correta pela mesma referência que entrou na lista: comparar
        // por texto re-traduzido não é determinístico.
        Collections.shuffle(opcoes);
        pergunta.setOpcoes(opcoes);
        pergunta.setRespostaCorreta(opcoes.indexOf(correta));

        return pergunta;
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return fallback;
        } catch (Exception e) {
            LOG.log(Level.FINE, "Tradução falhou - usando texto original", e);
            return fallback;
        }
    }

    /** Entidades HTML nomeadas que a API costuma devolver. */
    private static final Map<String, String> ENTIDADES = Map.ofEntries(
        Map.entry("&quot;", "\""),
        Map.entry("&apos;", "'"),
        Map.entry("&amp;", "&"),
        Map.entry("&lt;", "<"),
        Map.entry("&gt;", ">"),
        Map.entry("&nbsp;", " "),
        Map.entry("&deg;", "°"),
        Map.entry("&hellip;", "…"),
        Map.entry("&ldquo;", "“"),
        Map.entry("&rdquo;", "”"),
        Map.entry("&lsquo;", "‘"),
        Map.entry("&rsquo;", "’"),
        Map.entry("&eacute;", "é"),
        Map.entry("&egrave;", "è"),
        Map.entry("&ntilde;", "ñ"),
        Map.entry("&ccedil;", "ç"),
        Map.entry("&uuml;", "ü"),
        Map.entry("&ouml;", "ö"),
        Map.entry("&auml;", "ä")
    );

    /** &#8217; e &#x27; — qualquer entidade numérica, decimal ou hexadecimal. */
    private static final Pattern ENTIDADE_NUMERICA = Pattern.compile("&#(x?)([0-9A-Fa-f]+);");

    /**
     * Decodifica HTML entities (a API retorna texto codificado).
     * Visível para o pacote por causa do teste unitário.
     */
    static String decodeHtml(String text) {
        if (text == null) {
            return "";
        }

        // NÃO usar URLDecoder aqui: o texto da API não é URL-encoded.
        // URLDecoder quebra com '%' literal (IllegalArgumentException) e troca '+' por espaço.

        // Numéricas primeiro: cobre tudo que não está no mapa de nomes.
        Matcher m = ENTIDADE_NUMERICA.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            int base = m.group(1).isEmpty() ? 10 : 16;
            String substituto;
            try {
                substituto = Character.toString(Integer.parseInt(m.group(2), base));
            } catch (NumberFormatException e) {
                substituto = m.group(); // código absurdo: deixa como veio
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(substituto));
        }
        m.appendTail(sb);

        String resultado = sb.toString();
        // &amp; fica para o fim: senão "&amp;lt;" viraria "<" em vez de "&lt;"
        for (Map.Entry<String, String> entidade : ENTIDADES.entrySet()) {
            if (!"&amp;".equals(entidade.getKey())) {
                resultado = resultado.replace(entidade.getKey(), entidade.getValue());
            }
        }
        return resultado.replace("&amp;", "&");
    }

    /**
     * Registra um novo usuário no backend.
     * Ainda não usado: depende de um servidor em localhost:8080.
     *
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
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200 || response.statusCode() == 201;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "Registro interrompido", e);
            return false;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Erro ao registrar usuário", e);
            return false;
        }
    }

    /**
     * Autentica um usuário no backend.
     * Ainda não usado: depende de um servidor em localhost:8080.
     *
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
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Quando o backend existir, o corpo da resposta traz o usuário
                // real (com pontuação); por ora devolve o que foi enviado.
                return gson.fromJson(response.body(), Usuario.class);
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "Login interrompido", e);
            return null;
        } catch (IOException | JsonSyntaxException e) {
            LOG.log(Level.WARNING, "Erro ao fazer login", e);
            return null;
        }
    }

    /**
     * Verifica se um PIN é válido no backend.
     * Ainda não usado: depende de um servidor em localhost:8080.
     *
     * @param pin O PIN a ser verificado
     * @return true se o PIN é válido
     */
    public boolean validatePin(String pin) {
        try {
            HttpResponse<String> response = enviarGet(BASE_URL + "/quiz/validate-pin?pin="
                + URLEncoder.encode(pin, StandardCharsets.UTF_8));
            return response.statusCode() == 200;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "Validação de PIN interrompida", e);
            return false;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Erro ao validar PIN", e);
            return false;
        }
    }
}
