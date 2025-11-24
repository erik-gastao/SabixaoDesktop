module br.com.grupo2.sabixao.sabixao {
    requires javafx.controls;
    requires javafx.fxml;

    opens br.com.grupo2.sabixao.sabixao to javafx.fxml;
    exports br.com.grupo2.sabixao.sabixao;
}
