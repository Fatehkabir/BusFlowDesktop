module org.example.busflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    opens org.example.busflow.controller to javafx.fxml;
    exports org.example.busflow.controller;
    exports org.example.busflow;
    exports org.example.busflow.database;
}
