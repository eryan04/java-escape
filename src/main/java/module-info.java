module com.example.javaescape {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;


    opens com.example.javaescape to javafx.fxml;
    exports com.example.javaescape;
}
