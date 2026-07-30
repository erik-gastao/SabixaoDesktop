package br.com.grupo2.sabixao.sabixao.service;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serviço de tradução usando API pública do MyMemory
 * API gratuita sem necessidade de chave
 */
public class TranslatorService {

    private static final Logger LOG = Logger.getLogger(TranslatorService.class.getName());

    private static final String TRANSLATE_API = "https://api.mymemory.translated.net/get";
    private static final Duration TIMEOUT_CONEXAO = Duration.ofSeconds(5);
    private static final Duration TIMEOUT_REQUISICAO = Duration.ofSeconds(3);

    private final HttpClient httpClient;
    // Cache de traduções da sessão: evita repetir chamadas HTTP para o mesmo texto
    // (thread-safe — traduções rodam em paralelo no ApiService)
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public TranslatorService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT_CONEXAO)
            .build();
    }

    /**
     * Traduz texto do inglês para português
     * @param text Texto em inglês
     * @return Texto traduzido em português
     */
    public String translateToPortuguese(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Cache: mesmo texto nunca traduz duas vezes na sessão
        String cached = cache.get(text);
        if (cached != null) {
            return cached;
        }

        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            // '|' precisa ser %7C — caractere ilegal em URI.create (tradução nunca funcionou com ele)
            String url = TRANSLATE_API + "?q=" + encodedText + "&langpair=en%7Cpt-br";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(TIMEOUT_REQUISICAO)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                var jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                var responseData = jsonResponse.getAsJsonObject("responseData");
                String translatedText = clean(responseData.get("translatedText").getAsString());

                if (isUsableTranslation(translatedText, text)) {
                    cache.put(text, translatedText);
                    return translatedText;
                }
                LOG.log(Level.FINE, "Tradução inválida para \"{0}\" - usando original", text);
            } else {
                LOG.log(Level.FINE, "Tradutor respondeu HTTP {0}", response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.FINE, "Tradução interrompida", e);
            return text; // não cachear: a interrupção não diz nada sobre o texto
        } catch (IOException | JsonSyntaxException | IllegalStateException | NullPointerException e) {
            LOG.log(Level.FINE, "Erro ao traduzir - usando original", e);
        }

        cache.put(text, text); // Cachear a falha também: evita repetir timeout no mesmo texto
        return text; // Retorna original se a tradução falhar
    }

    /**
     * Descarta respostas que não servem: vazias, iguais ao original ou com o
     * aviso de cota do MyMemory.
     * Visível para o pacote por causa do teste unitário.
     */
    static boolean isUsableTranslation(String traducao, String original) {
        return traducao != null
            && !traducao.isEmpty()
            && !traducao.equals(original)
            && !traducao.contains("MYMEMORY WARNING");
    }

    /**
     * O MyMemory às vezes devolve a tradução prefixada com um travessão
     * ("- Qual é a montanha mais alta do mundo?"), que aparecia na tela.
     * Visível para o pacote por causa do teste unitário.
     */
    static String clean(String traducao) {
        if (traducao == null) {
            return null;
        }
        return traducao.replaceFirst("^\\s*[-–—]\\s*", "").trim();
    }
}
