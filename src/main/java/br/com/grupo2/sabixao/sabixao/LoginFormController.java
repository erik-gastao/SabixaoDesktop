package br.com.grupo2.sabixao.sabixao;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginFormController {

    @FXML
    private TextField nomeField;

    @FXML
    private PasswordField senhaField;

    /**
     * Valida os campos e faz login
     */
    @FXML
    private void handleEntrar() {
        String nome = nomeField.getText().trim();
        String senha = senhaField.getText();

        // Validações
        if (nome.isEmpty() || senha.isEmpty()) {
            showAlert("Campos Vazios", "Por favor, preencha todos os campos!", Alert.AlertType.WARNING);
            return;
        }

        if (nome.length() < 3) {
            showAlert("Nome Inválido", "O nome deve ter pelo menos 3 caracteres!", Alert.AlertType.WARNING);
            return;
        }

        if (senha.length() < 6) {
            showAlert("Senha Inválida", "A senha deve ter pelo menos 6 caracteres!", Alert.AlertType.WARNING);
            return;
        }

        // TODO: Integrar com backend/API para validar login
        System.out.println("Fazendo login - Nome: " + nome);
        showAlert("Sucesso", "Login realizado com sucesso!\nBem-vindo, " + nome + "!", Alert.AlertType.INFORMATION);
        
        // TODO: Navegar para tela do quiz após login bem-sucedido
        limparCampos();
    }

    /**
     * Navega para a tela de criar conta
     */
    @FXML
    private void handleCriarConta() {
        try {
            App.setRoot("criar-conta");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível abrir a tela de criar conta!", Alert.AlertType.ERROR);
        }
    }

    /**
     * Volta para a tela inicial
     */
    @FXML
    private void handleVoltar() {
        try {
            App.setRoot("home");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível voltar para a tela inicial!", Alert.AlertType.ERROR);
        }
    }

    /**
     * Limpa todos os campos do formulário
     */
    private void limparCampos() {
        nomeField.clear();
        senhaField.clear();
    }

    /**
     * Exibe alertas para o usuário
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
