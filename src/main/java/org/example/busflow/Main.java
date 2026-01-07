package org.example.busflow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.busflow.database.DatabaseManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseManager.initDatabase();

        Parent root = FXMLLoader.load(getClass().getResource("/org/example/busflow/login.fxml"));
        primaryStage.setTitle("BusFlow - Bus Ticket Management");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        System.out.println("✅ Application started");
        System.out.println("Default Admin: username=admin, password=admin123");
    }

    @Override
    public void stop() {
        DatabaseManager.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}