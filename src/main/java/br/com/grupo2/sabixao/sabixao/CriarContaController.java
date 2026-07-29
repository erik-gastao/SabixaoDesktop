package br.com.grupo2.sabixao.sabixao;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class CriarContaController {

    @FXML
    private TextField nomeField;

    @FXML
    private PasswordField senhaField;

    @FXML
    private PasswordField confirmarSenhaField;

    /**
     * Valida os campos e cria uma nova conta
     */
    @FXML
    private void aoCriarConta() {
        String nome = nomeField.getText().trim();
        String senha = senhaField.getText();
        String confirmarSenha = confirmarSenhaField.getText();

        // Validações
        if (nome.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            mostrarAlerta("Campos Vazios", "Por favor, preencha todos os campos!", Alert.AlertType.WARNING);
            return;
        }

        if (nome.length() < 3) {
            mostrarAlerta("Nome Inválido", "O nome deve ter pelo menos 3 caracteres!", Alert.AlertType.WARNING);
            return;
        }

        if (senha.length() < 6) {
            mostrarAlerta("Senha Fraca", "A senha deve ter pelo menos 6 caracteres!", Alert.AlertType.WARNING);
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            mostrarAlerta("Senhas Diferentes", "As senhas não coincidem!", Alert.AlertType.ERROR);
            return;
        }

        // TODO: Integrar com backend/API para salvar o usuário
        System.out.println("Criando conta - Nome: " + nome);
        mostrarAlerta("Sucesso", "Conta criada com sucesso para " + nome + "!\nVocê já pode fazer login.", Alert.AlertType.INFORMATION);
        
        // Após criar conta, redirecionar para tela de login
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
            limparCampos();
        }
    }

    /**
     * Volta para a tela de login
     */
    @FXML
    private void aoVoltar() {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível voltar para a tela de login!", Alert.AlertType.ERROR);
        }
    }

    /**
     * Limpa todos os campos do formulário
     */
    private void limparCampos() {
        nomeField.clear();
        senhaField.clear();
        confirmarSenhaField.clear();
    }

    /**
     * Exibe alertas para o usuário
     */
    private void mostrarAlerta(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
