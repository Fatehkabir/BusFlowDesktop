package org.example.busflow.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.busflow.database.DatabaseManager;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class UserController {

    private int userId;
    private String userName;

    @FXML private Label userNameLabel;
    @FXML private TextField sourceSearchField;
    @FXML private TextField destinationSearchField;

    @FXML private TableView<BusData> searchResultsTable;
    @FXML private TableView<TicketData> myTicketsTable;

    @FXML private VBox searchBusPane;
    @FXML private VBox myTicketsPane;
    @FXML private VBox profilePane;

    @FXML private Label profileNameLabel;
    @FXML private Label profileUsernameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profilePhoneLabel;
    @FXML private Label profileBookingsLabel;

    private final ObservableList<BusData> busesData = FXCollections.observableArrayList();
    private final ObservableList<TicketData> ticketsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTables();
        showSearchBus(null);
    }

    public void setUserData(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        if (userNameLabel != null) {
            userNameLabel.setText(userName);
        }
        loadUserProfile();
        loadMyTickets();
    }

    private void setupTables() {
        setupSearchTable();
        setupTicketsTable();
    }

    private void setupSearchTable() {
        if (searchResultsTable == null) return;
        searchResultsTable.getColumns().clear();

        TableColumn<BusData, String> col1 = new TableColumn<>("Bus Number");
        col1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().busNumber));

        TableColumn<BusData, String> col2 = new TableColumn<>("Bus Name");
        col2.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().busName));

        TableColumn<BusData, String> col3 = new TableColumn<>("Route");
        col3.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().route));

        TableColumn<BusData, String> col4 = new TableColumn<>("Departure");
        col4.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().departureTime));

        TableColumn<BusData, String> col5 = new TableColumn<>("Available Seats");
        col5.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().availableSeats));

        TableColumn<BusData, String> col6 = new TableColumn<>("Fare");
        col6.setCellValueFactory(data -> new SimpleStringProperty("৳" + data.getValue().fare));

        searchResultsTable.getColumns().addAll(col1, col2, col3, col4, col5, col6);
        searchResultsTable.setItems(busesData);
    }

    private void setupTicketsTable() {
        if (myTicketsTable == null) return;
        myTicketsTable.getColumns().clear();

        TableColumn<TicketData, String> col1 = new TableColumn<>("Ticket ID");
        col1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().ticketId));

        TableColumn<TicketData, String> col2 = new TableColumn<>("Bus");
        col2.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().busInfo));

        TableColumn<TicketData, String> col3 = new TableColumn<>("Route");
        col3.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().route));

        TableColumn<TicketData, String> col4 = new TableColumn<>("Journey Date");
        col4.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().journeyDate));

        TableColumn<TicketData, String> col5 = new TableColumn<>("Seats");
        col5.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().seats));

        TableColumn<TicketData, String> col6 = new TableColumn<>("Fare");
        col6.setCellValueFactory(data -> new SimpleStringProperty("৳" + data.getValue().fare));

        TableColumn<TicketData, String> col7 = new TableColumn<>("Status");
        col7.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status));

        myTicketsTable.getColumns().addAll(col1, col2, col3, col4, col5, col6, col7);
        myTicketsTable.setItems(ticketsData);
    }

    @FXML
    private void handleSearch() {
        String source = sourceSearchField.getText().trim();
        String destination = destinationSearchField.getText().trim();

        busesData.clear();
        try {
            ResultSet rs = DatabaseManager.searchBuses(source, destination);
            while (rs != null && rs.next()) {
                busesData.add(new BusData(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("bus_number"),
                        rs.getString("bus_name"),
                        rs.getString("source") + " → " + rs.getString("destination"),
                        rs.getString("departure_time"),
                        String.valueOf(rs.getInt("available_seats")),
                        String.format("%.2f", rs.getDouble("fare"))
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBookTicket() {
        BusData selected = searchResultsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a bus", Alert.AlertType.WARNING);
            return;
        }

        if (selected.availableSeats.equals("0")) {
            showAlert("Error", "No seats available", Alert.AlertType.WARNING);
            return;
        }

        showBookingDialog(selected);
    }

    private void showBookingDialog(BusData bus) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Book Ticket");
        dialog.setHeaderText("Booking for: " + bus.busName);

        ButtonType bookButtonType = new ButtonType("Confirm Booking", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(bookButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker journeyDatePicker = new DatePicker();
        journeyDatePicker.setValue(LocalDate.now());
        journeyDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        TextField seatsField = new TextField("1");
        Label totalLabel = new Label("Total: ৳0.00");

        seatsField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (!newVal.isEmpty()) {
                    int seats = Integer.parseInt(newVal);
                    double fare = Double.parseDouble(bus.fare);
                    totalLabel.setText("Total: ৳" + String.format("%.2f", seats * fare));
                }
            } catch (NumberFormatException e) {
                totalLabel.setText("Total: ৳0.00");
            }
        });

        seatsField.setText("1");

        grid.add(new Label("Bus Route:"), 0, 0);
        grid.add(new Label(bus.route), 1, 0);
        grid.add(new Label("Journey Date:*"), 0, 1);
        grid.add(journeyDatePicker, 1, 1);
        grid.add(new Label("Number of Seats:*"), 0, 2);
        grid.add(seatsField, 1, 2);
        grid.add(totalLabel, 0, 3, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == bookButtonType) {
            try {
                LocalDate journeyDate = journeyDatePicker.getValue();
                int seats = Integer.parseInt(seatsField.getText().trim());
                double farePerSeat = Double.parseDouble(bus.fare);
                double totalFare = seats * farePerSeat;

                if (journeyDate == null) {
                    showAlert("Error", "Please select journey date", Alert.AlertType.ERROR);
                    return;
                }

                String journeyDateStr = journeyDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

                boolean success = DatabaseManager.bookTicket(userId, Integer.parseInt(bus.id), journeyDateStr, seats, totalFare);

                if (success) {
                    showAlert("Success", "Ticket booked successfully!\nTotal: ৳" + String.format("%.2f", totalFare), Alert.AlertType.INFORMATION);
                    handleSearch();
                    loadMyTickets();
                } else {
                    showAlert("Error", "Booking failed. Not enough seats.", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid input", Alert.AlertType.ERROR);
            }
        }
    }

    private void loadMyTickets() {
        ticketsData.clear();
        try {
            ResultSet rs = DatabaseManager.getUserTickets(userId);
            while (rs != null && rs.next()) {
                String statusText = rs.getString("status");
                String statusEmoji = statusText.equals("confirmed") ? "✅ " : "❌ ";

                ticketsData.add(new TicketData(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("bus_name") + " (" + rs.getString("bus_number") + ")",
                        rs.getString("source") + " → " + rs.getString("destination"),
                        rs.getString("journey_date"),
                        String.valueOf(rs.getInt("seats_booked")),
                        String.format("%.2f", rs.getDouble("total_fare")),
                        statusEmoji + statusText.toUpperCase()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (profileBookingsLabel != null) {
            profileBookingsLabel.setText(String.valueOf(ticketsData.size()));
        }
    }

    private void loadUserProfile() {
        try {
            String query = "SELECT * FROM users WHERE id = ?";
            java.sql.PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next() && profileNameLabel != null) {
                profileNameLabel.setText(rs.getString("full_name"));
                profileUsernameLabel.setText("@" + rs.getString("username"));
                profileEmailLabel.setText(rs.getString("email") != null ? rs.getString("email") : "Not provided");
                profilePhoneLabel.setText(rs.getString("phone") != null ? rs.getString("phone") : "Not provided");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelTicket() {
        TicketData selected = myTicketsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a ticket", Alert.AlertType.WARNING);
            return;
        }

        if (selected.status.contains("CANCELLED")) {
            showAlert("Info", "Ticket already cancelled", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Ticket");
        confirm.setContentText("Cancel ticket #" + selected.ticketId + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = DatabaseManager.cancelTicket(Integer.parseInt(selected.ticketId));
                if (success) {
                    showAlert("Success", "Ticket cancelled!", Alert.AlertType.INFORMATION);
                    loadMyTickets();
                }
            }
        });
    }

    @FXML
    private void showSearchBus(javafx.event.ActionEvent event) {
        hideAll();
        if (searchBusPane != null) searchBusPane.setVisible(true);
    }

    @FXML
    private void showMyTickets(javafx.event.ActionEvent event) {
        hideAll();
        if (myTicketsPane != null) myTicketsPane.setVisible(true);
        loadMyTickets();
    }

    @FXML
    private void showProfile(javafx.event.ActionEvent event) {
        hideAll();
        if (profilePane != null) profilePane.setVisible(true);
        loadUserProfile();
    }

    private void hideAll() {
        if (searchBusPane != null) searchBusPane.setVisible(false);
        if (myTicketsPane != null) myTicketsPane.setVisible(false);
        if (profilePane != null) profilePane.setVisible(false);
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/claudeproject/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BusFlow - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class BusData {
        public String id, busNumber, busName, route, departureTime, availableSeats, fare;

        public BusData(String id, String busNumber, String busName, String route,
                       String departureTime, String availableSeats, String fare) {
            this.id = id;
            this.busNumber = busNumber;
            this.busName = busName;
            this.route = route;
            this.departureTime = departureTime;
            this.availableSeats = availableSeats;
            this.fare = fare;
        }
    }

    public static class TicketData {
        public String ticketId, busInfo, route, journeyDate, seats, fare, status;

        public TicketData(String ticketId, String busInfo, String route, String journeyDate,
                          String seats, String fare, String status) {
            this.ticketId = ticketId;
            this.busInfo = busInfo;
            this.route = route;
            this.journeyDate = journeyDate;
            this.seats = seats;
            this.fare = fare;
            this.status = status;
        }
    }
}