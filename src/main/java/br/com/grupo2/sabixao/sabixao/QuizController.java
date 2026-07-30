package br.com.grupo2.sabixao.sabixao;

import br.com.grupo2.sabixao.sabixao.model.Pergunta;
import br.com.grupo2.sabixao.sabixao.service.ApiService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller para a tela do quiz
 * Integrado com Open Trivia Database API
 */
public class QuizController {

    private static final Logger LOG = Logger.getLogger(QuizController.class.getName());

    /** Tempo de cada pergunta. O timer e o cycleCount da Timeline derivam daqui. */
    private static final int TEMPO_POR_PERGUNTA = 30;
    private static final int TOTAL_PERGUNTAS = 10;
    private static final String DIFICULDADE = "medium";
    private static final int PONTOS_BASE = 100;
    private static final int PONTOS_POR_SEGUNDO_RESTANTE = 2;
    /** Pausa mostrando o feedback antes de trocar de pergunta. */
    private static final Duration PAUSA_ENTRE_PERGUNTAS = Duration.seconds(2);
    /** Marcador de resposta quando o tempo acaba sem escolha. */
    private static final int SEM_RESPOSTA = -1;

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

    private List<Pergunta> perguntas;
    private int perguntaAtual = 0;
    private int pontuacao = 0;
    private int tempoRestante = TEMPO_POR_PERGUNTA;
    private Timeline timeline;
    private String nomeJogador;
    private String pin;
    private ApiService apiService;

    /**
     * Inicia o quiz com os dados do jogador.
     * Nome diferente de initialize() de propósito: o FXMLLoader chama
     * initialize() sozinho e sem argumentos ao carregar a tela.
     */
    public void iniciar(String nome, String pin) {
        this.nomeJogador = nome;
        this.pin = pin;
        this.perguntaAtual = 0;
        this.pontuacao = 0;
        this.apiService = new ApiService();

        carregarPerguntas();
    }

    /**
     * Busca as perguntas da API sem travar a interface.
     *
     * Usa javafx.concurrent.Task em vez de Thread crua: os handlers
     * onSucceeded/onFailed já rodam na thread do JavaFX, o que dispensa
     * Platform.runLater e garante que falha inesperada não passe em silêncio.
     */
    private void carregarPerguntas() {
        mostrarCarregamento();

        Task<List<Pergunta>> busca = new Task<>() {
            @Override
            protected List<Pergunta> call() {
                LOG.log(Level.INFO, "Buscando {0} perguntas (dificuldade {1})",
                    new Object[] {TOTAL_PERGUNTAS, DIFICULDADE});
                return apiService.fetchTriviaQuestions(TOTAL_PERGUNTAS, DIFICULDADE, null);
            }
        };

        busca.setOnSucceeded(e -> {
            List<Pergunta> resultado = busca.getValue();
            if (resultado == null || resultado.isEmpty()) {
                LOG.warning("API não retornou perguntas - usando exemplos locais");
                mostrarInfo("Modo Offline\n\nNão foi possível conectar com a API externa.\n"
                    + "Usando perguntas de exemplo em português.");
                perguntas = criarPerguntasExemplo();
            } else {
                LOG.log(Level.INFO, "{0} perguntas carregadas da API", resultado.size());
                perguntas = resultado;
            }
            carregarPergunta();
        });

        busca.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Erro ao consultar a API", busca.getException());
            mostrarInfo("Modo Offline\n\nErro ao conectar com a API.\n"
                + "Usando perguntas de exemplo para demonstração.");
            perguntas = criarPerguntasExemplo();
            carregarPergunta();
        });

        Thread t = new Thread(busca, "busca-perguntas");
        t.setDaemon(true); // não segura o JVM se a janela fechar durante a busca
        t.start();
    }

    /**
     * Mostra mensagem de carregamento
     */
    private void mostrarCarregamento() {
        perguntaLabel.setText("Conectando com a API externa...\n\nAguarde alguns segundos");
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
     * Carrega a pergunta atual na tela
     */
    private void carregarPergunta() {
        if (perguntaAtual >= perguntas.size()) {
            finalizarQuiz();
            return;
        }

        Pergunta pergunta = perguntas.get(perguntaAtual);

        perguntaLabel.setText(pergunta.getTexto());
        perguntaNumeroLabel.setText("Pergunta " + (perguntaAtual + 1) + " de " + perguntas.size());
        pontuacaoLabel.setText("Pontuação: " + pontuacao);

        // Atualizar botões com as opções — esconder botões sem opção
        // (perguntas boolean da API têm só 2 alternativas)
        List<String> opcoes = pergunta.getOpcoes();
        Button[] botoes = {opcaoAButton, opcaoBButton, opcaoCButton, opcaoDButton};
        for (int i = 0; i < botoes.length; i++) {
            boolean temOpcao = i < opcoes.size();
            botoes[i].setVisible(temOpcao);
            botoes[i].setManaged(temOpcao);
            if (temOpcao) {
                botoes[i].setText((char) ('A' + i) + ") " + opcoes.get(i));
            }
        }

        habilitarBotoes(true);
        iniciarTimer();
    }

    /**
     * Manipula a seleção de uma opção
     */
    @FXML
    private void aoEscolherOpcaoA() {
        verificarResposta(0);
    }

    @FXML
    private void aoEscolherOpcaoB() {
        verificarResposta(1);
    }

    @FXML
    private void aoEscolherOpcaoC() {
        verificarResposta(2);
    }

    @FXML
    private void aoEscolherOpcaoD() {
        verificarResposta(3);
    }

    /**
     * Verifica se a resposta está correta
     */
    private void verificarResposta(int opcaoSelecionada) {
        pararTimer();
        habilitarBotoes(false);

        Pergunta pergunta = perguntas.get(perguntaAtual);
        boolean acertou = pergunta.verificarResposta(opcaoSelecionada);

        if (acertou) {
            pontuacao += calcularPontos(tempoRestante);
            mostrarFeedback("Correto!", true);
        } else {
            String correta = pergunta.getOpcoes().get(pergunta.getRespostaCorreta());
            String prefixo = opcaoSelecionada == SEM_RESPOSTA ? "Tempo esgotado!" : "Incorreto!";
            mostrarFeedback(prefixo + "\nResposta correta: " + correta, false);
        }

        Timeline delay = new Timeline(new KeyFrame(PAUSA_ENTRE_PERGUNTAS, e -> {
            perguntaAtual++;
            carregarPergunta();
        }));
        delay.play();
    }

    /**
     * Pontuação da pergunta: base fixa mais bônus pelo tempo que sobrou.
     * Estático e visível para o pacote por causa do teste unitário.
     */
    static int calcularPontos(int tempoRestante) {
        return PONTOS_BASE + Math.max(0, tempoRestante) * PONTOS_POR_SEGUNDO_RESTANTE;
    }

    /**
     * Inicia o timer da pergunta
     */
    private void iniciarTimer() {
        pararTimer(); // descarta uma Timeline anterior que ainda estivesse viva

        tempoRestante = TEMPO_POR_PERGUNTA;
        tempoProgressBar.setProgress(1.0);
        tempoLabel.setText(tempoRestante + "s");

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempoRestante--;
            tempoLabel.setText(tempoRestante + "s");
            tempoProgressBar.setProgress((double) tempoRestante / TEMPO_POR_PERGUNTA);

            if (tempoRestante <= 0) {
                verificarResposta(SEM_RESPOSTA);
            }
        }));
        timeline.setCycleCount(TEMPO_POR_PERGUNTA);
        timeline.play();
    }

    /**
     * Para o timer
     */
    private void pararTimer() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
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

        Timeline autoClose = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> alert.close()));
        autoClose.play();
    }

    /**
     * Finaliza o quiz e mostra resultado
     */
    private void finalizarQuiz() {
        pararTimer();

        // Libera as threads de tradução do ApiService desta partida: sem isso cada
        // quiz jogado deixava um pool de 8 threads para trás.
        if (apiService != null) {
            apiService.close();
            apiService = null;
        }

        int pontuacaoFinal = pontuacao;
        int totalRespondidas = perguntas.size();

        // Platform.runLater é obrigatório aqui: finalizarQuiz é alcançado de dentro
        // do KeyFrame da Timeline de pausa e showAndWait() lança
        // IllegalStateException("showAndWait is not allowed during animation or
        // layout processing") se chamado durante o pulso de animação — o quiz
        // travava na última pergunta sem nunca exibir o resultado.
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Quiz Finalizado!");
            alert.setHeaderText("Parabéns, " + nomeJogador + "!");
            alert.setContentText("Sua pontuação final foi: " + pontuacaoFinal + " pontos!\n\n"
                + "Perguntas respondidas: " + totalRespondidas + "\n"
                + "PIN da partida: " + pin);
            alert.showAndWait();

            try {
                App.setRoot("inicio");
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Falha ao voltar para a tela inicial", e);
            }
        });
    }

    /**
     * Cria perguntas de exemplo para teste
     */
    private List<Pergunta> criarPerguntasExemplo() {
        List<Pergunta> exemplos = new ArrayList<>();

        Pergunta q1 = new Pergunta();
        q1.setTexto("Qual é a capital do Brasil?");
        q1.setOpcoes(List.of("São Paulo", "Rio de Janeiro", "Brasília", "Salvador"));
        q1.setRespostaCorreta(2);
        exemplos.add(q1);

        Pergunta q2 = new Pergunta();
        q2.setTexto("Quanto é 2 + 2?");
        q2.setOpcoes(List.of("3", "4", "5", "6"));
        q2.setRespostaCorreta(1);
        exemplos.add(q2);

        Pergunta q3 = new Pergunta();
        q3.setTexto("Qual linguagem é usada para desenvolvimento Android nativo?");
        q3.setOpcoes(List.of("Swift", "Kotlin", "Python", "Ruby"));
        q3.setRespostaCorreta(1);
        exemplos.add(q3);

        return exemplos;
    }
}
