package br.com.grupo2.sabixao.sabixao.service;

import br.com.grupo2.sabixao.sabixao.model.User;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Serviço para comunicação com a API/Backend
 * Esta classe será usada para integrar com o backend quando estiver pronto
 */
public class ApiService {
    
    private static final String BASE_URL = "http://localhost:8080/api"; // Alterar conforme necessário
    private final HttpClient httpClient;

    public ApiService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Registra um novo usuário no sistema
     * @param user O usuário a ser registrado
     * @return true se o registro foi bem-sucedido
     */
    public boolean registerUser(User user) {
        try {
            String jsonBody = String.format("{\"nome\":\"%s\",\"senha\":\"%s\"}", 
                user.getNome(), user.getSenha());

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
    public User loginUser(String nome, String senha) {
        try {
            String jsonBody = String.format("{\"nome\":\"%s\",\"senha\":\"%s\"}", nome, senha);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // TODO: Parse JSON response para criar objeto User
                return new User(nome, senha);
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
                .uri(URI.create(BASE_URL + "/quiz/validate-pin?pin=" + pin))
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
