module br.com.grupo2.sabixao.sabixao {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;

    opens br.com.grupo2.sabixao.sabixao to javafx.fxml;
    opens br.com.grupo2.sabixao.sabixao.model to com.google.gson;
    
    exports br.com.grupo2.sabixao.sabixao;
    exports br.com.grupo2.sabixao.sabixao.model;
    exports br.com.grupo2.sabixao.sabixao.service;
}
