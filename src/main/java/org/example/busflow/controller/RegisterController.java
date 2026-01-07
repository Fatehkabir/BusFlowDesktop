package org.example.busflow.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.busflow.database.DatabaseManager;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label messageLabel;
    @FXML private Button registerButton;
    @FXML private Button cancelButton;

    @FXML
    public void initialize() {
        registerButton.setOnAction(e -> handleRegister());
        cancelButton.setOnAction(e -> handleCancel());
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            messageLabel.setText("⚠️ Please fill all required fields");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (username.length() < 3) {
            messageLabel.setText("⚠️ Username must be at least 3 characters");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (password.length() < 6) {
            messageLabel.setText("⚠️ Password must be at least 6 characters");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("⚠️ Passwords do not match");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!email.isEmpty() && !email.contains("@")) {
            messageLabel.setText("⚠️ Please enter a valid email address");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Register user
        boolean success = DatabaseManager.registerUser(username, password, fullName, email, phone);

        if (success) {
            messageLabel.setText("✅ Registration successful! You can now login.");
            messageLabel.setStyle("-fx-text-fill: green;");

            clearFields();

            // Auto close after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        Stage stage = (Stage) registerButton.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            messageLabel.setText("❌ Registration failed. Username may already exist.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        nameField.clear();
        emailField.clear();
        phoneField.clear();
    }
}