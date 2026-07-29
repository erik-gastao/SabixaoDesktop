package br.com.grupo2.sabixao.sabixao.service;

import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço de tradução usando API pública do MyMemory
 * API gratuita sem necessidade de chave
 */
public class TranslatorService {
    
    private static final String TRANSLATE_API = "https://api.mymemory.translated.net/get";
    private final HttpClient httpClient;
    // Cache de traduções da sessão: evita repetir chamadas HTTP para o mesmo texto
    // (thread-safe — traduções rodam em paralelo no ApiService)
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public TranslatorService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
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

        System.out.println("    🔄 Traduzindo: \"" + text.substring(0, Math.min(40, text.length())) + "...\"");

        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            // '|' precisa ser %7C — caractere ilegal em URI.create (tradução nunca funcionou com ele)
            String url = TRANSLATE_API + "?q=" + encodedText + "&langpair=en%7Cpt-br";
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(3))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Parse JSON response
                var jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                var responseData = jsonResponse.getAsJsonObject("responseData");
                String translatedText = responseData.get("translatedText").getAsString();
                
                // Verificar se a tradução é válida
                if (translatedText != null && 
                    !translatedText.isEmpty() &&
                    !translatedText.equals(text) &&
                    !translatedText.contains("MYMEMORY WARNING")) {
                    
                    System.out.println("    ✅ Traduzido: \"" + translatedText.substring(0, Math.min(40, translatedText.length())) + "...\"");
                    cache.put(text, translatedText);
                    return translatedText;
                } else {
                    System.out.println("    ⚠️ Tradução inválida, usando original");
                }
            } else {
                System.out.println("    ❌ Status HTTP: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("    ❌ Erro ao traduzir: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
        
        System.out.println("    📝 Usando texto original");
        cache.put(text, text); // Cachear a falha também: evita repetir timeout de 3s no mesmo texto
        return text; // Retorna original se a tradução falhar
    }
}
