package br.com.grupo2.sabixao.sabixao;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import java.io.IOException;

public class HomeController {

    @FXML
    private TextField nomeField;

    @FXML
    private TextField pinField;

    /**
     * Valida os campos e inicia o jogo/quiz
     */
    @FXML
    private void handleComecar() {
        String nome = nomeField.getText().trim();
        String pin = pinField.getText().trim();

        if (nome.isEmpty() || pin.isEmpty()) {
            showAlert("Campos Vazios", "Por favor, preencha todos os campos!");
            return;
        }

        if (!isValidPin(pin)) {
            showAlert("PIN Inválido", "O PIN deve conter apenas números!");
            return;
        }

        // TODO: Implementar lógica para iniciar o jogo com o PIN
        System.out.println("Iniciando jogo - Nome: " + nome + ", PIN: " + pin);
        showAlert("Sucesso", "Iniciando o jogo para " + nome + "!");
    }

    /**
     * Navega para a tela de login/cadastro
     */
    @FXML
    private void handleLogin() {
        try {
            App.setRoot("login-form");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível abrir a tela de login!");
        }
    }

    /**
     * Valida se o PIN contém apenas números
     */
    private boolean isValidPin(String pin) {
        return pin.matches("\\d+");
    }

    /**
     * Exibe alertas para o usuário
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
