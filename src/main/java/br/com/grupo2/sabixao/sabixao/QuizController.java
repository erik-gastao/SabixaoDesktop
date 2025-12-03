package br.com.grupo2.sabixao.sabixao;

import br.com.grupo2.sabixao.sabixao.model.Question;
import br.com.grupo2.sabixao.sabixao.service.ApiService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller para a tela do quiz
 * Integrado com Open Trivia Database API
 */
public class QuizController {

    @FXML
    private Label perguntaLabel;

    @FXML
    private Button opcaoAButton;

    @FXML
    private Button opcaoBButton;

    @FXML
    private Button opcaoCButton;

    @FXML
    private Button opcaoDButton;

    @FXML
    private Label pontuacaoLabel;

    @FXML
    private Label perguntaNumeroLabel;

    @FXML
    private ProgressBar tempoProgressBar;

    @FXML
    private Label tempoLabel;

    private List<Question> perguntas;
    private int perguntaAtual = 0;
    private int pontuacao = 0;
    private int tempoRestante = 30; // segundos
    private Timeline timeline;
    private String nomeJogador;
    private String pin;
    private ApiService apiService;

    /**
     * Inicializa o quiz com os dados do jogador
     */
    public void initialize(String nome, String pin) {
        this.nomeJogador = nome;
        this.pin = pin;
        this.perguntaAtual = 0;
        this.pontuacao = 0;
        this.apiService = new ApiService();

        // Buscar perguntas da API em segundo plano
        carregarPerguntasAPI();
    }

    /**
     * Carrega perguntas da API externa
     */
    private void carregarPerguntasAPI() {
        mostrarCarregamento();
        
        // Executar em thread separada para não travar a UI
        new Thread(() -> {
            try {
                System.out.println("\n=== TENTANDO BUSCAR PERGUNTAS DA API ===");
                System.out.println("URL: https://opentrivia.com/api.php?amount=10&difficulty=medium&type=multiple");
                
                // Buscar 10 perguntas de dificuldade média
                perguntas = apiService.fetchTriviaQuestions(10, "medium", null);
                
                Platform.runLater(() -> {
                    if (perguntas == null || perguntas.isEmpty()) {
                        System.out.println("❌ API retornou lista vazia - usando perguntas de exemplo");
                        mostrarInfo("⚠️ Modo Offline\n\nNão foi possível conectar com a API externa.\nUsando perguntas de exemplo em português.");
                        perguntas = criarPerguntasExemplo();
                    } else {
                        System.out.println("✅ API FUNCIONOU! " + perguntas.size() + " perguntas carregadas!");
                        mostrarSucesso("✅ API Externa Conectada!\n\n" + perguntas.size() + " perguntas reais carregadas.\n\nNOTA: Perguntas em inglês (API internacional).");
                    }
                    carregarPergunta();
                });
            } catch (Exception e) {
                System.err.println("❌ ERRO AO CONECTAR COM API: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    mostrarInfo("⚠️ Modo Offline\n\nErro ao conectar: " + e.getMessage() + "\n\nUsando perguntas de exemplo para demonstração.");
                    perguntas = criarPerguntasExemplo();
                    carregarPergunta();
                });
            }
        }).start();
    }

    /**
     * Mostra mensagem de carregamento
     */
    private void mostrarCarregamento() {
        perguntaLabel.setText("🌐 Conectando com API externa...\n\nAguarde alguns segundos");
        habilitarBotoes(false);
    }

    /**
     * Mostra mensagem de informação
     */
    private void mostrarInfo(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.show();
        
        Timeline autoClose = new Timeline(new KeyFrame(Duration.seconds(3), e -> alert.close()));
        autoClose.play();
    }

    /**
     * Mostra mensagem de sucesso
     */
    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso!");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.show();
        
        Timeline autoClose = new Timeline(new KeyFrame(Duration.seconds(2), e -> alert.close()));
        autoClose.play();
    }

    /**
     * Mostra mensagem de erro
     */
    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.show();
        
        Timeline autoClose = new Timeline(new KeyFrame(Duration.seconds(2), e -> alert.close()));
        autoClose.play();
    }

    /**
     * Carrega a pergunta atual na tela
     */
    private void carregarPergunta() {
        if (perguntaAtual >= perguntas.size()) {
            finalizarQuiz();
            return;
        }

        Question pergunta = perguntas.get(perguntaAtual);

        // Atualizar labels
        perguntaLabel.setText(pergunta.getTexto());
        perguntaNumeroLabel.setText("Pergunta " + (perguntaAtual + 1) + " de " + perguntas.size());
        pontuacaoLabel.setText("Pontuação: " + pontuacao);

        // Atualizar botões com as opções
        List<String> opcoes = pergunta.getOpcoes();
        opcaoAButton.setText("A) " + opcoes.get(0));
        opcaoBButton.setText("B) " + opcoes.get(1));
        opcaoCButton.setText("C) " + opcoes.get(2));
        opcaoDButton.setText("D) " + opcoes.get(3));

        // Habilitar botões
        habilitarBotoes(true);

        // Iniciar timer
        iniciarTimer();
    }

    /**
     * Manipula a seleção de uma opção
     */
    @FXML
    private void handleOpcaoA() {
        verificarResposta(0);
    }

    @FXML
    private void handleOpcaoB() {
        verificarResposta(1);
    }

    @FXML
    private void handleOpcaoC() {
        verificarResposta(2);
    }

    @FXML
    private void handleOpcaoD() {
        verificarResposta(3);
    }

    /**
     * Verifica se a resposta está correta
     */
    private void verificarResposta(int opcaoSelecionada) {
        pararTimer();
        habilitarBotoes(false);

        Question pergunta = perguntas.get(perguntaAtual);
        boolean acertou = pergunta.verificarResposta(opcaoSelecionada);

        if (acertou) {
            pontuacao += calcularPontos();
            mostrarFeedback("Correto! 🎉", true);
        } else {
            mostrarFeedback("Incorreto! ❌\nResposta correta: " + 
                pergunta.getOpcoes().get(pergunta.getRespostaCorreta()), false);
        }

        // Aguardar 2 segundos e carregar próxima pergunta
        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            perguntaAtual++;
            carregarPergunta();
        }));
        delay.play();
    }

    /**
     * Calcula pontos baseado no tempo restante
     */
    private int calcularPontos() {
        int pontosBase = 100;
        int bonusTempo = tempoRestante * 2; // 2 pontos por segundo restante
        return pontosBase + bonusTempo;
    }

    /**
     * Inicia o timer da pergunta
     */
    private void iniciarTimer() {
        tempoRestante = 30;
        tempoProgressBar.setProgress(1.0);
        tempoLabel.setText(tempoRestante + "s");

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempoRestante--;
            tempoLabel.setText(tempoRestante + "s");
            tempoProgressBar.setProgress((double) tempoRestante / 30);

            if (tempoRestante <= 0) {
                verificarResposta(-1); // Tempo esgotado = resposta errada
            }
        }));
        timeline.setCycleCount(30);
        timeline.play();
    }

    /**
     * Para o timer
     */
    private void pararTimer() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    /**
     * Habilita ou desabilita os botões de resposta
     */
    private void habilitarBotoes(boolean habilitar) {
        opcaoAButton.setDisable(!habilitar);
        opcaoBButton.setDisable(!habilitar);
        opcaoCButton.setDisable(!habilitar);
        opcaoDButton.setDisable(!habilitar);
    }

    /**
     * Mostra feedback visual da resposta
     */
    private void mostrarFeedback(String mensagem, boolean acertou) {
        Alert alert = new Alert(acertou ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.setTitle(acertou ? "Correto!" : "Incorreto");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.show();

        // Fechar automaticamente
        Timeline autoClose = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> alert.close()));
        autoClose.play();
    }

    /**
     * Finaliza o quiz e mostra resultado
     */
    private void finalizarQuiz() {
        pararTimer();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Quiz Finalizado!");
        alert.setHeaderText("Parabéns, " + nomeJogador + "!");
        alert.setContentText("Sua pontuação final foi: " + pontuacao + " pontos!\n\n" +
                "Perguntas respondidas: " + perguntas.size());
        alert.showAndWait();

        // TODO: Salvar pontuação no backend
        // TODO: Voltar para tela inicial ou ranking

        try {
            App.setRoot("home");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cria perguntas de exemplo para teste
     */
    private List<Question> criarPerguntasExemplo() {
        List<Question> exemplos = new ArrayList<>();

        Question q1 = new Question();
        q1.setTexto("Qual é a capital do Brasil?");
        q1.setOpcoes(List.of("São Paulo", "Rio de Janeiro", "Brasília", "Salvador"));
        q1.setRespostaCorreta(2);
        exemplos.add(q1);

        Question q2 = new Question();
        q2.setTexto("Quanto é 2 + 2?");
        q2.setOpcoes(List.of("3", "4", "5", "6"));
        q2.setRespostaCorreta(1);
        exemplos.add(q2);

        Question q3 = new Question();
        q3.setTexto("Qual linguagem é usada para desenvolvimento Android nativo?");
        q3.setOpcoes(List.of("Swift", "Kotlin", "Python", "Ruby"));
        q3.setRespostaCorreta(1);
        exemplos.add(q3);

        return exemplos;
    }
}
