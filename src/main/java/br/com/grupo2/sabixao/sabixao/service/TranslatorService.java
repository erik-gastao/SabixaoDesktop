package br.com.grupo2.sabixao.sabixao.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço de tradução usando API pública do MyMemory
 * API gratuita sem necessidade de chave
 */
public class TranslatorService {
    
    private static final String TRANSLATE_API = "https://api.mymemory.translated.net/get";
    private final HttpClient httpClient;
    private final Map<String, String> commonTranslations;
    
    public TranslatorService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();
        
        // Dicionário de traduções comuns para fallback
        this.commonTranslations = new HashMap<>();
        initCommonTranslations();
    }
    
    private void initCommonTranslations() {
        // Palavras e frases comuns em perguntas
        commonTranslations.put("Which", "Qual");
        commonTranslations.put("What", "O que");
        commonTranslations.put("Who", "Quem");
        commonTranslations.put("Where", "Onde");
        commonTranslations.put("When", "Quando");
        commonTranslations.put("How", "Como");
        commonTranslations.put("Why", "Por que");
        commonTranslations.put("wrote", "escreveu");
        commonTranslations.put("author", "autor");
        commonTranslations.put("book", "livro");
        commonTranslations.put("year", "ano");
        commonTranslations.put("country", "país");
        commonTranslations.put("city", "cidade");
        commonTranslations.put("capital", "capital");
        commonTranslations.put("president", "presidente");
        commonTranslations.put("war", "guerra");
        commonTranslations.put("True", "Verdadeiro");
        commonTranslations.put("False", "Falso");
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
        
        System.out.println("    🔄 Traduzindo: \"" + text.substring(0, Math.min(40, text.length())) + "...\"");
        
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = TRANSLATE_API + "?q=" + encodedText + "&langpair=en|pt-br";
            
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
        return text; // Retorna original se a tradução falhar
    }
    
    /**
     * Traduz múltiplas strings (otimizado para lotes pequenos)
     */
    public String[] translateBatch(String[] texts) {
        String[] translated = new String[texts.length];
        
        for (int i = 0; i < texts.length; i++) {
            translated[i] = translateToPortuguese(texts[i]);
            
            // Pequeno delay para não sobrecarregar a API gratuita
            if (i < texts.length - 1) {
                try {
                    Thread.sleep(200); // 200ms entre traduções
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        return translated;
    }
}
