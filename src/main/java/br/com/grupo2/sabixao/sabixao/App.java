package br.com.grupo2.sabixao.sabixao;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("inicio"), 720, 480);
        stage.setScene(scene);
        stage.setTitle("Sabixão - Quiz Game");
        stage.setMinWidth(720);
        stage.setMinHeight(480);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        setRoot(fxml, null);
    }

    /**
     * Troca a tela e aplica uma configuração no controller antes de exibi-la.
     * Usado para passar dados entre telas (ex.: nome e PIN para o quiz).
     */
    static <T> void setRoot(String fxml, Consumer<T> configurarController) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent root = fxmlLoader.load();

        if (configurarController != null) {
            configurarController.accept(fxmlLoader.getController());
        }

        scene.setRoot(root);
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}