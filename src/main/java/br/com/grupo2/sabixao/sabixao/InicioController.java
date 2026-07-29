package br.com.grupo2.sabixao.sabixao;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import java.io.IOException;

public class InicioController {

    @FXML
    private TextField nomeField;

    @FXML
    private TextField pinField;

    /**
     * Valida os campos e inicia o jogo/quiz
     */
    @FXML
    private void aoComecar() {
        String nome = nomeField.getText().trim();
        String pin = pinField.getText().trim();

        if (nome.isEmpty() || pin.isEmpty()) {
            mostrarAlerta("Campos Vazios", "Por favor, preencha todos os campos!");
            return;
        }

        if (!pinValido(pin)) {
            mostrarAlerta("PIN Inválido", "O PIN deve conter apenas números!");
            return;
        }

        // Iniciar o quiz - irá buscar perguntas da API externa
        System.out.println("Iniciando jogo - Nome: " + nome + ", PIN: " + pin);

        try {
            App.setRoot("quiz", (QuizController controller) -> controller.iniciar(nome, pin));
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível iniciar o quiz: " + e.getMessage());
        }
    }

    /**
     * Navega para a tela de login/cadastro
     */
    @FXML
    private void aoAbrirLogin() {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir a tela de login!");
        }
    }

    /**
     * Valida se o PIN contém apenas números
     */
    private boolean pinValido(String pin) {
        return pin.matches("\\d+");
    }

    /**
     * Exibe alertas para o usuário
     */
    private void mostrarAlerta(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
