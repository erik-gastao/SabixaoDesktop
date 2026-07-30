package br.com.grupo2.sabixao.sabixao;

import br.com.grupo2.sabixao.sabixao.model.Usuario;
import br.com.grupo2.sabixao.sabixao.service.LocalAuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {

    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());

    @FXML
    private TextField nomeField;

    @FXML
    private PasswordField senhaField;

    private final LocalAuthService auth = LocalAuthService.getInstance();

    /**
     * Valida os campos e autentica contra o cadastro local.
     *
     * Antes esta tela anunciava "Login realizado com sucesso" sem conferir nada:
     * qualquer nome com 3+ letras e qualquer senha com 6+ caracteres entravam.
     */
    @FXML
    private void aoEntrar() {
        String nome = nomeField.getText().trim();
        String senha = senhaField.getText();

        if (nome.isEmpty() || senha.isEmpty()) {
            mostrarAlerta("Campos Vazios", "Por favor, preencha todos os campos!", Alert.AlertType.WARNING);
            return;
        }

        Optional<Usuario> usuario = auth.authenticate(nome, senha);
        if (usuario.isEmpty()) {
            String detalhe = auth.exists(nome)
                ? "Senha incorreta."
                : "Não existe conta com esse nome. Crie uma conta primeiro.";
            mostrarAlerta("Login Falhou", detalhe, Alert.AlertType.ERROR);
            senhaField.clear();
            return;
        }

        LOG.log(Level.INFO, "Login efetuado: {0}", nome);
        mostrarAlerta("Sucesso", "Bem-vindo, " + usuario.get().getNome() + "!", Alert.AlertType.INFORMATION);
        limparCampos();

        // Leva o nome autenticado para a tela inicial já preenchido, para o
        // jogador não digitar de novo antes de começar o quiz.
        try {
            App.setRoot("inicio", (InicioController inicio) -> inicio.preencherNome(usuario.get().getNome()));
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Falha ao voltar para a tela inicial", e);
            mostrarAlerta("Erro", "Não foi possível abrir a tela inicial!", Alert.AlertType.ERROR);
        }
    }

    /**
     * Navega para a tela de criar conta
     */
    @FXML
    private void aoAbrirCriarConta() {
        try {
            App.setRoot("criar-conta");
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Falha ao abrir criar-conta", e);
            mostrarAlerta("Erro", "Não foi possível abrir a tela de criar conta!", Alert.AlertType.ERROR);
        }
    }

    /**
     * Volta para a tela inicial
     */
    @FXML
    private void aoVoltar() {
        try {
            App.setRoot("inicio");
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Falha ao voltar para inicio", e);
            mostrarAlerta("Erro", "Não foi possível voltar para a tela inicial!", Alert.AlertType.ERROR);
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
    private void mostrarAlerta(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
