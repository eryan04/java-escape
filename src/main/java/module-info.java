module com.example.javaescape {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.javaescape to javafx.fxml;
    exports com.example.javaescape;
}