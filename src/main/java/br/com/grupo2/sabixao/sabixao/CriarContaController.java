package br.com.grupo2.sabixao.sabixao;

import br.com.grupo2.sabixao.sabixao.service.LocalAuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CriarContaController {

    private static final Logger LOG = Logger.getLogger(CriarContaController.class.getName());

    @FXML
    private TextField nomeField;

    @FXML
    private PasswordField senhaField;

    @FXML
    private PasswordField confirmarSenhaField;

    private final LocalAuthService auth = LocalAuthService.getInstance();

    /**
     * Valida os campos e cria a conta no cadastro local.
     *
     * Antes só validava o formato e dizia "conta criada" sem guardar nada — dava
     * para "criar" a mesma conta infinitas vezes e nenhuma delas servia no login.
     */
    @FXML
    private void aoCriarConta() {
        String nome = nomeField.getText().trim();
        String senha = senhaField.getText();
        String confirmarSenha = confirmarSenhaField.getText();

        if (nome.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            mostrarAlerta("Campos Vazios", "Por favor, preencha todos os campos!", Alert.AlertType.WARNING);
            return;
        }

        if (nome.length() < LocalAuthService.MIN_NAME_LENGTH) {
            mostrarAlerta("Nome Inválido",
                "O nome deve ter pelo menos " + LocalAuthService.MIN_NAME_LENGTH + " caracteres!",
                Alert.AlertType.WARNING);
            return;
        }

        if (senha.length() < LocalAuthService.MIN_PASSWORD_LENGTH) {
            mostrarAlerta("Senha Fraca",
                "A senha deve ter pelo menos " + LocalAuthService.MIN_PASSWORD_LENGTH + " caracteres!",
                Alert.AlertType.WARNING);
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            mostrarAlerta("Senhas Diferentes", "As senhas não coincidem!", Alert.AlertType.ERROR);
            return;
        }

        if (!auth.register(nome, senha)) {
            mostrarAlerta("Nome Em Uso", "Já existe uma conta com o nome \"" + nome + "\".", Alert.AlertType.ERROR);
            return;
        }

        LOG.log(Level.INFO, "Conta criada: {0}", nome);
        mostrarAlerta("Sucesso", "Conta criada para " + nome + "!\nVocê já pode fazer login.",
            Alert.AlertType.INFORMATION);

        try {
            App.setRoot("login");
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Falha ao abrir login apos criar conta", e);
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
            LOG.log(Level.SEVERE, "Falha ao voltar para login", e);
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
