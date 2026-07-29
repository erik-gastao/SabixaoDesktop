package br.com.grupo2.sabixao.sabixao;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Garante que toda tela carrega de verdade.
 *
 * O compilador não valida FXML: fx:id sem campo correspondente e onAction
 * apontando para método inexistente só estouram em runtime, ao carregar a
 * tela. Como todo handler foi renomeado (handleOpcaoA -> aoEscolherOpcaoA
 * e afins), é este teste que garante que FXML e controller seguem casados.
 */
class CarregamentoTelasTest {

    @BeforeAll
    static void iniciarJavaFx() throws InterruptedException {
        CountDownLatch pronto = new CountDownLatch(1);
        try {
            Platform.startup(pronto::countDown);
        } catch (IllegalStateException jaIniciado) {
            pronto.countDown();
        }
        if (!pronto.await(10, TimeUnit.SECONDS)) {
            fail("JavaFX não inicializou em 10 segundos");
        }
    }

    @ParameterizedTest(name = "{0}.fxml carrega sem erro")
    @ValueSource(strings = {"inicio", "login", "criar-conta", "quiz"})
    void telaCarrega(String tela) throws Exception {
        AtomicReference<Throwable> falha = new AtomicReference<>();
        AtomicReference<Object> raiz = new AtomicReference<>();
        CountDownLatch fim = new CountDownLatch(1);

        // FXMLLoader precisa rodar na thread do JavaFX
        Platform.runLater(() -> {
            try {
                raiz.set(new FXMLLoader(App.class.getResource(tela + ".fxml")).load());
            } catch (Throwable t) {
                falha.set(t);
            } finally {
                fim.countDown();
            }
        });

        if (!fim.await(20, TimeUnit.SECONDS)) {
            fail("Timeout ao carregar " + tela + ".fxml");
        }
        if (falha.get() != null) {
            throw new AssertionError("Falha ao carregar " + tela + ".fxml", falha.get());
        }
        assertNotNull(raiz.get(), tela + ".fxml carregou nulo");
    }

    /**
     * Confere se todo <Image url="@..."/> aponta para um arquivo que existe.
     *
     * Carregar o FXML não basta: a JavaFX Image não lança quando o recurso
     * some, ela só marca erro internamente e a tela abre sem a imagem. Foi
     * exatamente assim que quiz.fxml passou a apontar para uma pasta
     * inexistente e ninguém percebeu.
     */
    @ParameterizedTest(name = "{0}.fxml: imagens existem")
    @ValueSource(strings = {"inicio", "login", "criar-conta", "quiz"})
    void imagensExistem(String tela) throws Exception {
        URL fxml = App.class.getResource(tela + ".fxml");
        assertNotNull(fxml, tela + ".fxml não encontrado");

        String conteudo = new String(fxml.openStream().readAllBytes(), StandardCharsets.UTF_8);
        Matcher m = Pattern.compile("<Image\\s+url=\"@([^\"]+)\"").matcher(conteudo);

        int encontradas = 0;
        while (m.find()) {
            String caminho = m.group(1);
            encontradas++;
            URI destino = fxml.toURI().resolve(caminho);
            try (var in = destino.toURL().openStream()) {
                assertNotNull(in);
            } catch (Exception e) {
                fail(tela + ".fxml aponta para imagem inexistente: " + caminho);
            }
        }
        assertTrue(encontradas > 0, tela + ".fxml não declara nenhuma imagem");
    }
}
