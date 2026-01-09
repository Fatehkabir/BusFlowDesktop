package org.example.busflow.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.busflow.database.DatabaseManager;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AdminController {

    @FXML private Label adminNameLabel;
    @FXML private TextField busIdField;
    @FXML private TextField busNameField;
    @FXML private TextField sourceCityField;
    @FXML private TextField destCityField;
    @FXML private TextField departureTimeField;
    @FXML private TextField arrivalTimeField;
    @FXML private TextField totalSeatsField;
    @FXML private TextField fareField;

    @FXML private TableView<BusData> busesTable;
    @FXML private TableView<BookingData> bookingsTable;
    @FXML private TableView<CustomerData> customersTable;

    @FXML private VBox addBusPane;
    @FXML private VBox bookingsPane;
    @FXML private VBox customersPane;
    @FXML private VBox dashboardPane;
    @FXML private Button addBusButton;
    @FXML private Button updateBusButton;
    @FXML private Button deleteBusButton;

    private final ObservableList<BusData> busesData = FXCollections.observableArrayList();
    private final ObservableList<BookingData> bookingsData = FXCollections.observableArrayList();
    private final ObservableList<CustomerData> customersData = FXCollections.observableArrayList();
    @FXML private Label totalBusesLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalBookingsLabel;
    @FXML private Label totalRevenueLabel;

    @FXML private PieChart bookingStatusChart;
    @FXML private BarChart<String, Number> routeRevenueChart;

    private int selectedBusId = -1;

    @FXML
    public void initialize() {
        setupTables();
        loadAllData();
        showDashboard(null);
    }

    public void setAdminName(String name) {
        if (adminNameLabel != null) {
            adminNameLabel.setText(name);
        }
    }

    private void setupTables() {
        setupBusesTable();
        setupBookingsTable();
        setupCustomersTable();
    }

    private void setupBusesTable() {
        if (busesTable == null) return;
        busesTable.getColumns().clear();

        TableColumn<BusData, String> col1 = new TableColumn<>("Bus ID");
        col1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().busId));
        col1.setPrefWidth(60);

        TableColumn<BusData, String> col2 = new TableColumn<>("Bus Number");
        col2.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().busNumber));
        col2.setPrefWidth(120);

        TableColumn<BusData, String> col3 = new TableColumn<>("Bus Name");
        col3.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().busName));
        col3.setPrefWidth(120);

        TableColumn<BusData, String> col4 = new TableColumn<>("Route");
        col4.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().route));
        col4.setPrefWidth(200);

        TableColumn<BusData, String> col5 = new TableColumn<>("Time");
        col5.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().time));
        col5.setPrefWidth(150);

        TableColumn<BusData, String> col6 = new TableColumn<>("Seats");
        col6.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().seats));
        col6.setPrefWidth(100);

        TableColumn<BusData, String> col7 = new TableColumn<>("Fare");
        col7.setCellValueFactory(data -> new SimpleStringProperty("৳" + data.getValue().fare));
        col7.setPrefWidth(80);

        busesTable.getColumns().addAll(col1, col2, col3, col4, col5, col6, col7);
        busesTable.setItems(busesData);

        busesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    selectedBusId = Integer.parseInt(newVal.busId);
                } catch (NumberFormatException e) {
                    selectedBusId = -1;
                }
                populateBusForm(newVal);
            }
        });
    }

    private void setupBookingsTable() {
        if (bookingsTable == null) return;
        bookingsTable.getColumns().clear();

        TableColumn<BookingData, String> col1 = new TableColumn<>("Ticket ID");
        col1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().ticketId));

        TableColumn<BookingData, String> col2 = new TableColumn<>("User");
        col2.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().userName));

        TableColumn<BookingData, String> col3 = new TableColumn<>("Bus");
        col3.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().busName));

        TableColumn<BookingData, String> col4 = new TableColumn<>("Route");
        col4.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().route));

        TableColumn<BookingData, String> col5 = new TableColumn<>("Journey Date");
        col5.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().journeyDate));

        TableColumn<BookingData, String> col6 = new TableColumn<>("Seats");
        col6.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().seats));

        TableColumn<BookingData, String> col7 = new TableColumn<>("Fare");
        col7.setCellValueFactory(data -> new SimpleStringProperty("৳" + data.getValue().fare));

        TableColumn<BookingData, String> col8 = new TableColumn<>("Status");
        col8.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status));

        bookingsTable.getColumns().addAll(col1, col2, col3, col4, col5, col6, col7, col8);
        bookingsTable.setItems(bookingsData);
    }

    private void setupCustomersTable() {
        if (customersTable == null) return;
        customersTable.getColumns().clear();

        TableColumn<CustomerData, String> col1 = new TableColumn<>("ID");
        col1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().id));

        TableColumn<CustomerData, String> col2 = new TableColumn<>("Full Name");
        col2.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().fullName));

        TableColumn<CustomerData, String> col3 = new TableColumn<>("Username");
        col3.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().username));

        TableColumn<CustomerData, String> col4 = new TableColumn<>("Email");
        col4.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().email));

        TableColumn<CustomerData, String> col5 = new TableColumn<>("Phone");
        col5.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().phone));

        customersTable.getColumns().addAll(col1, col2, col3, col4, col5);
        customersTable.setItems(customersData);
    }

    private void loadAllData() {
        loadBusData();
        loadBookingsData();
        loadCustomersData();
        loadStatistics();
        loadCharts();
    }

    private void loadBusData() {
        busesData.clear();
        try {
            ResultSet rs = DatabaseManager.getAllBuses();
            while (rs != null && rs.next()) {
                busesData.add(new BusData(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("bus_number"),
                        rs.getString("bus_name"),
                        rs.getString("source") + " → " + rs.getString("destination"),
                        rs.getString("departure_time") + " - " + rs.getString("arrival_time"),
                        rs.getInt("available_seats") + "/" + rs.getInt("total_seats"),
                        String.format("%.2f", rs.getDouble("fare")),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getString("departure_time"),
                        rs.getString("arrival_time"),
                        String.valueOf(rs.getInt("total_seats"))
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBookingsData() {
        bookingsData.clear();
        try {
            ResultSet rs = DatabaseManager.getAllTickets();
            while (rs != null && rs.next()) {
                bookingsData.add(new BookingData(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("full_name"),
                        rs.getString("bus_name"),
                        rs.getString("source") + " → " + rs.getString("destination"),
                        rs.getString("journey_date"),
                        String.valueOf(rs.getInt("seats_booked")),
                        String.format("%.2f", rs.getDouble("total_fare")),
                        rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCustomersData() {
        customersData.clear();
        try {
            ResultSet rs = DatabaseManager.getAllUsers();
            while (rs != null && rs.next()) {
                customersData.add(new CustomerData(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("full_name"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("phone")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStatistics() {
        if (totalBusesLabel != null) {
            totalBusesLabel.setText(String.valueOf(DatabaseManager.getTotalBuses()));
        }
        if (totalUsersLabel != null) {
            totalUsersLabel.setText(String.valueOf(DatabaseManager.getTotalUsers()));
        }
        if (totalBookingsLabel != null) {
            totalBookingsLabel.setText(String.valueOf(DatabaseManager.getTotalBookings()));
        }
        if (totalRevenueLabel != null) {
            totalRevenueLabel.setText(String.format("৳%.2f", DatabaseManager.getTotalRevenue()));
        }
    }

    private void loadCharts() {
        loadBookingStatusChart();
        loadRouteRevenueChart();
    }

    private void loadBookingStatusChart() {
        if (bookingStatusChart == null) return;

        try {
            Map<String, Integer> statusCount = DatabaseManager.getBookingStatusCount();

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            for (Map.Entry<String, Integer> entry : statusCount.entrySet()) {
                pieChartData.add(new PieChart.Data(
                        entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1) + " (" + entry.getValue() + ")",
                        entry.getValue()
                ));
            }

            bookingStatusChart.setData(pieChartData);
            bookingStatusChart.setLegendVisible(true);
            bookingStatusChart.setLabelsVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRouteRevenueChart() {
        if (routeRevenueChart == null) return;

        try {
            Map<String, Double> routeRevenue = DatabaseManager.getRouteRevenue();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue by Route");

            for (Map.Entry<String, Double> entry : routeRevenue.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            routeRevenueChart.getData().clear();
            routeRevenueChart.getData().add(series);
            routeRevenueChart.setLegendVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateBusForm(BusData bus) {
        if (bus == null) return;
        busIdField.setText(bus.busNumber);
        busNameField.setText(bus.busName);
        sourceCityField.setText(bus.source);
        destCityField.setText(bus.destination);
        departureTimeField.setText(bus.departureTime);
        arrivalTimeField.setText(bus.arrivalTime);
        totalSeatsField.setText(bus.totalSeats);
        fareField.setText(bus.fare);
    }

    @FXML
    private void handleAddBus() {
        try {
            String busNumber = busIdField.getText().trim();
            String busName = busNameField.getText().trim();
            String source = sourceCityField.getText().trim();
            String dest = destCityField.getText().trim();
            String departure = departureTimeField.getText().trim();
            String arrival = arrivalTimeField.getText().trim();
            int seats = Integer.parseInt(totalSeatsField.getText().trim());
            double fare = Double.parseDouble(fareField.getText().trim());

            if (busNumber.isEmpty() || busName.isEmpty()) {
                showAlert("Error", "Please fill all required fields", Alert.AlertType.ERROR);
                return;
            }

            boolean success = DatabaseManager.addBus(busNumber, busName, source, dest, departure, arrival, seats, fare);
            if (success) {
                showAlert("Success", "Bus added successfully!", Alert.AlertType.INFORMATION);
                clearBusFields();
                loadBusData();
                loadStatistics();
                loadCharts();
            } else {
                showAlert("Error", "Failed to add bus. Bus number may already exist.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid input for seats or fare!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdateBus() {
        if (selectedBusId == -1) {
            showAlert("Error", "Please select a bus to update", Alert.AlertType.WARNING);
            return;
        }

        try {
            String busNumber = busIdField.getText().trim();
            String busName = busNameField.getText().trim();
            String source = sourceCityField.getText().trim();
            String dest = destCityField.getText().trim();
            String departure = departureTimeField.getText().trim();
            String arrival = arrivalTimeField.getText().trim();
            int seats = Integer.parseInt(totalSeatsField.getText().trim());
            double fare = Double.parseDouble(fareField.getText().trim());

            boolean success = DatabaseManager.updateBus(selectedBusId, busNumber, busName, source, dest, departure, arrival, seats, fare);
            if (success) {
                showAlert("Success", "Bus updated successfully!", Alert.AlertType.INFORMATION);
                loadBusData();
                loadStatistics();
                loadCharts();
            } else {
                showAlert("Error", "Failed to update bus", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid input for seats or fare!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteBus() {
        BusData selected = busesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a bus to delete", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Bus: " + selected.busName);
        confirm.setContentText("Are you sure?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = DatabaseManager.deleteBus(Integer.parseInt(selected.busId));
                if (success) {
                    showAlert("Success", "Bus deleted successfully!", Alert.AlertType.INFORMATION);
                    clearBusFields();
                    loadBusData();
                    loadStatistics();
                    loadCharts();
                } else {
                    showAlert("Error", "Failed to delete bus", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleUpdateBooking() {
        BookingData selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        showAlert("Info", "Booking updated for Ticket ID: " + selected.ticketId, Alert.AlertType.INFORMATION);
        bookingsTable.refresh();
    }

    @FXML
    private void handleCancelBooking() {
        BookingData selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a booking to cancel", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Booking");
        confirm.setContentText("Cancel ticket #" + selected.ticketId + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = DatabaseManager.cancelTicket(Integer.parseInt(selected.ticketId));
                if (success) {
                    showAlert("Success", "Booking cancelled successfully!", Alert.AlertType.INFORMATION);
                    loadBookingsData();
                    loadStatistics();
                    loadCharts();
                }
            }
        });
    }

    @FXML
    private void handleUpdateCustomer() {
        CustomerData selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a customer to update", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.fullName);
        dialog.setTitle("Update Customer");
        dialog.setHeaderText("Update: " + selected.username);
        dialog.setContentText("Full Name:");

        dialog.showAndWait().ifPresent(name -> {
            boolean success = DatabaseManager.updateUser(Integer.parseInt(selected.id), name, selected.email, selected.phone);
            if (success) {
                showAlert("Success", "Customer updated successfully!", Alert.AlertType.INFORMATION);
                loadCustomersData();
            }
        });
    }

    @FXML
    private void handleDeleteCustomer() {
        CustomerData selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a customer to delete", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Customer");
        confirm.setContentText("Delete customer: " + selected.fullName + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = DatabaseManager.deleteUser(Integer.parseInt(selected.id));
                if (success) {
                    showAlert("Success", "Customer deleted successfully!", Alert.AlertType.INFORMATION);
                    loadCustomersData();
                    loadStatistics();
                }
            }
        });
    }

    @FXML
    private void showDashboard(javafx.event.ActionEvent event) {
        hideAll();
        if (dashboardPane != null) dashboardPane.setVisible(true);
        loadStatistics();
        loadCharts();
    }

    @FXML
    private void showAddBus(javafx.event.ActionEvent event) {
        hideAll();
        if (addBusPane != null) addBusPane.setVisible(true);
        loadBusData();
    }

    @FXML
    private void showBookings(javafx.event.ActionEvent event) {
        hideAll();
        if (bookingsPane != null) bookingsPane.setVisible(true);
        loadBookingsData();
    }

    @FXML
    private void showCustomers(javafx.event.ActionEvent event) {
        hideAll();
        if (customersPane != null) customersPane.setVisible(true);
        loadCustomersData();
    }

    private void hideAll() {
        if (dashboardPane != null) dashboardPane.setVisible(false);
        if (addBusPane != null) addBusPane.setVisible(false);
        if (bookingsPane != null) bookingsPane.setVisible(false);
        if (customersPane != null) customersPane.setVisible(false);
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/busflow/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BusFlow - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearBusFields() {
        busIdField.clear();
        busNameField.clear();
        sourceCityField.clear();
        destCityField.clear();
        departureTimeField.clear();
        arrivalTimeField.clear();
        totalSeatsField.clear();
        fareField.clear();
        selectedBusId = -1;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class BusData {
        public String busId, busNumber, busName, route, time, seats, fare;
        public String source, destination, departureTime, arrivalTime, totalSeats;

        public BusData(String busId, String busNumber, String busName, String route, String time,
                       String seats, String fare, String source, String destination,
                       String departureTime, String arrivalTime, String totalSeats) {
            this.busId = busId;
            this.busNumber = busNumber;
            this.busName = busName;
            this.route = route;
            this.time = time;
            this.seats = seats;
            this.fare = fare;
            this.source = source;
            this.destination = destination;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.totalSeats = totalSeats;
        }
    }

    public static class BookingData {
        public String ticketId, userName, busName, route, journeyDate, seats, fare, status;

        public BookingData(String ticketId, String userName, String busName, String route,
                           String journeyDate, String seats, String fare, String status) {
            this.ticketId = ticketId;
            this.userName = userName;
            this.busName = busName;
            this.route = route;
            this.journeyDate = journeyDate;
            this.seats = seats;
            this.fare = fare;
            this.status = status;
        }
    }

    public static class CustomerData {
        public String id, fullName, username, email, phone;

        public CustomerData(String id, String fullName, String username, String email, String phone) {
            this.id = id;
            this.fullName = fullName;
            this.username = username;
            this.email = email;
            this.phone = phone;
        }
    }
}