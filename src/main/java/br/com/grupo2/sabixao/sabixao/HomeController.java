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

        // Iniciar o quiz - irá buscar perguntas da API externa
        System.out.println("Iniciando jogo - Nome: " + nome + ", PIN: " + pin);
        
        try {
            // Carregar tela do quiz
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("quiz.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            // Passar dados para o QuizController
            QuizController controller = loader.getController();
            controller.initialize(nome, pin);
            
            // Trocar a cena
            javafx.stage.Stage stage = (javafx.stage.Stage) nomeField.getScene().getWindow();
            stage.getScene().setRoot(root);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível iniciar o quiz: " + e.getMessage());
        }
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
