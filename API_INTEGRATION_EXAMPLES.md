# 🔄 Exemplos de Integração com API

## Como usar o ApiService nos Controllers

### LoginController - Exemplo Completo com API

```java
package br.com.grupo2.sabixao.sabixao;

import br.com.grupo2.sabixao.sabixao.model.User;
import br.com.grupo2.sabixao.sabixao.service.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField nomeField;

    @FXML
    private PasswordField senhaField;

    @FXML
    private PasswordField confirmarSenhaField;

    // Instância do serviço de API
    private final ApiService apiService = new ApiService();

    @FXML
    private void handleCriarConta() {
        String nome = nomeField.getText().trim();
        String senha = senhaField.getText();
        String confirmarSenha = confirmarSenhaField.getText();

        // Validações locais primeiro
        if (nome.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            showAlert("Campos Vazios", "Por favor, preencha todos os campos!", Alert.AlertType.WARNING);
            return;
        }

        if (nome.length() < 3) {
            showAlert("Nome Inválido", "O nome deve ter pelo menos 3 caracteres!", Alert.AlertType.WARNING);
            return;
        }

        if (senha.length() < 6) {
            showAlert("Senha Fraca", "A senha deve ter pelo menos 6 caracteres!", Alert.AlertType.WARNING);
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            showAlert("Senhas Diferentes", "As senhas não coincidem!", Alert.AlertType.ERROR);
            return;
        }

        // Criar usuário e enviar para API
        User user = new User(nome, senha);
        boolean success = apiService.registerUser(user);
        
        if (success) {
            showAlert("Sucesso", "Conta criada com sucesso para " + nome + "!", Alert.AlertType.INFORMATION);
            limparCampos();
        } else {
            showAlert("Erro", "Não foi possível criar a conta. Verifique se o nome já existe ou se o backend está rodando.", Alert.AlertType.ERROR);
        }
    }

    // ... resto do código
}
```

### HomeController - Exemplo com Validação de PIN via API

```java
package br.com.grupo2.sabixao.sabixao;

import br.com.grupo2.sabixao.sabixao.service.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import java.io.IOException;

public class HomeController {

    @FXML
    private TextField nomeField;

    @FXML
    private TextField pinField;

    private final ApiService apiService = new ApiService();

    @FXML
    private void handleComecar() {
        String nome = nomeField.getText().trim();
        String pin = pinField.getText().trim();

        // Validações locais
        if (nome.isEmpty() || pin.isEmpty()) {
            showAlert("Campos Vazios", "Por favor, preencha todos os campos!");
            return;
        }

        if (!isValidPin(pin)) {
            showAlert("PIN Inválido", "O PIN deve conter apenas números!");
            return;
        }

        // Validar PIN com a API
        boolean pinValido = apiService.validatePin(pin);
        
        if (pinValido) {
            showAlert("Sucesso", "PIN válido! Iniciando o jogo para " + nome + "!");
            // TODO: Navegar para tela do quiz
        } else {
            showAlert("PIN Inválido", "O PIN informado não existe ou está inativo. Verifique se o backend está rodando.");
        }
    }

    private boolean isValidPin(String pin) {
        return pin.matches("\\d+");
    }

    // ... resto do código
}
```

## 🔧 Tratamento de Erros Avançado

### ApiService com melhor tratamento de erros

```java
package br.com.grupo2.sabixao.sabixao.service;

import br.com.grupo2.sabixao.sabixao.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class ApiService {
    
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final Gson gson;

    public ApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    /**
     * Registra um novo usuário
     */
    public ApiResponse<User> registerUser(User user) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("nome", user.getNome());
            body.put("senha", user.getSenha());
            
            String jsonBody = gson.toJson(body);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                User createdUser = gson.fromJson(response.body(), User.class);
                return new ApiResponse<>(true, createdUser, "Usuário criado com sucesso");
            } else {
                JsonObject error = gson.fromJson(response.body(), JsonObject.class);
                String message = error.has("message") ? error.get("message").getAsString() : "Erro desconhecido";
                return new ApiResponse<>(false, null, message);
            }
        } catch (IOException | InterruptedException e) {
            return new ApiResponse<>(false, null, "Erro de conexão: " + e.getMessage());
        }
    }

    /**
     * Faz login do usuário
     */
    public ApiResponse<User> loginUser(String nome, String senha) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("nome", nome);
            body.put("senha", senha);
            
            String jsonBody = gson.toJson(body);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                User user = gson.fromJson(response.body(), User.class);
                return new ApiResponse<>(true, user, "Login realizado com sucesso");
            } else {
                return new ApiResponse<>(false, null, "Credenciais inválidas");
            }
        } catch (IOException | InterruptedException e) {
            return new ApiResponse<>(false, null, "Erro de conexão: " + e.getMessage());
        }
    }

    /**
     * Valida PIN do quiz
     */
    public ApiResponse<Boolean> validatePin(String pin) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/validate-pin?pin=" + pin))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            boolean isValid = response.statusCode() == 200;
            String message = isValid ? "PIN válido" : "PIN inválido";
            return new ApiResponse<>(isValid, isValid, message);
            
        } catch (IOException | InterruptedException e) {
            return new ApiResponse<>(false, false, "Erro de conexão: " + e.getMessage());
        }
    }

    /**
     * Classe para encapsular respostas da API
     */
    public static class ApiResponse<T> {
        private final boolean success;
        private final T data;
        private final String message;

        public ApiResponse(boolean success, T data, String message) {
            this.success = success;
            this.data = data;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public T getData() {
            return data;
        }

        public String getMessage() {
            return message;
        }
    }
}
```

### Exemplo de uso com ApiResponse

```java
@FXML
private void handleCriarConta() {
    // ... validações ...

    User user = new User(nome, senha);
    ApiService.ApiResponse<User> response = apiService.registerUser(user);
    
    if (response.isSuccess()) {
        showAlert("Sucesso", response.getMessage(), Alert.AlertType.INFORMATION);
        limparCampos();
    } else {
        showAlert("Erro", response.getMessage(), Alert.AlertType.ERROR);
    }
}
```

## 🎯 Modo Offline (Sem Backend)

Para desenvolvimento sem backend, você pode usar dados mockados:

```java
public class MockApiService extends ApiService {
    
    private Map<String, User> users = new HashMap<>();
    
    @Override
    public ApiResponse<User> registerUser(User user) {
        if (users.containsKey(user.getNome())) {
            return new ApiResponse<>(false, null, "Usuário já existe");
        }
        users.put(user.getNome(), user);
        return new ApiResponse<>(true, user, "Usuário criado com sucesso");
    }
    
    @Override
    public ApiResponse<User> loginUser(String nome, String senha) {
        User user = users.get(nome);
        if (user != null && user.getSenha().equals(senha)) {
            return new ApiResponse<>(true, user, "Login realizado");
        }
        return new ApiResponse<>(false, null, "Credenciais inválidas");
    }
    
    @Override
    public ApiResponse<Boolean> validatePin(String pin) {
        // Aceita qualquer PIN para teste
        return new ApiResponse<>(true, true, "PIN válido");
    }
}
```

## 🔄 Alternando entre Mock e API Real

```java
public class ServiceFactory {
    private static final boolean USE_MOCK = true; // Altere para false quando o backend estiver pronto
    
    public static ApiService createApiService() {
        if (USE_MOCK) {
            return new MockApiService();
        }
        return new ApiService();
    }
}

// Uso nos controllers:
private final ApiService apiService = ServiceFactory.createApiService();
```
