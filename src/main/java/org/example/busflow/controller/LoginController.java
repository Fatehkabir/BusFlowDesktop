package org.example.busflow.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.busflow.database.DatabaseManager;

import java.io.IOException;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        passwordField.setOnAction((e) -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("⚠️ Please enter username and password");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            ResultSet rs = DatabaseManager.authenticateUser(username, password);
            if (rs != null && rs.next()) {
                int userId = rs.getInt("id");
                String fullName = rs.getString("full_name");
                String role = rs.getString("role");

                Stage stage = (Stage) usernameField.getScene().getWindow();

                if (role.equals("admin")) {
                    loadAdminPanel(stage, fullName);
                } else {
                    loadUserPanel(stage, userId, fullName);
                }
            } else {
                messageLabel.setText("❌ Invalid username or password");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception ex) {
            messageLabel.setText("❌ Login failed: " + ex.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/busflow/register.fxml"));
            Parent root = loader.load();

            Stage registerStage = new Stage();
            registerStage.setTitle("User Registration");
            registerStage.setScene(new Scene(root));
            registerStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open registration form", Alert.AlertType.ERROR);
        }
    }

    private void loadAdminPanel(Stage stage, String name) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/busflow/admin.fxml"));
            Parent root = loader.load();

            org.example.busflow.controller.AdminController controller = loader.getController();
            controller.setAdminName(name);  // Now works

            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard - BusFlow");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load admin panel", Alert.AlertType.ERROR);
        }
    }

    private void loadUserPanel(Stage stage, int userId, String name) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/busflow/user.fxml"));
            Parent root = loader.load();

            org.example.busflow.controller.UserController controller = loader.getController();
            controller.setUserData(userId, name);

            stage.setScene(new Scene(root));
            stage.setTitle("User Panel - BusFlow");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load user panel", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
