package br.com.grupo2.sabixao.sabixao;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InicioController {

    private static final Logger LOG = Logger.getLogger(InicioController.class.getName());

    @FXML
    private TextField nomeField;

    @FXML
    private TextField pinField;

    /**
     * Preenche o nome vindo de outra tela (ex.: após login bem-sucedido).
     */
    public void preencherNome(String nome) {
        nomeField.setText(nome);
        pinField.requestFocus();
    }

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

        LOG.log(Level.INFO, "Iniciando jogo - nome={0}, pin={1}", new Object[] {nome, pin});

        try {
            App.setRoot("quiz", (QuizController controller) -> controller.iniciar(nome, pin));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Falha ao iniciar o quiz", e);
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
            LOG.log(Level.SEVERE, "Falha ao abrir login", e);
            mostrarAlerta("Erro", "Não foi possível abrir a tela de login!");
        }
    }

    /**
     * Valida se o PIN contém apenas números.
     * Visível para o pacote por causa do teste unitário.
     */
    static boolean pinValido(String pin) {
        return pin != null && pin.matches("\\d+");
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
