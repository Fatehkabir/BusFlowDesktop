package org.example.busflow.database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:busflow.db";
    private static Connection conn = null;

    public static void initDatabase() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTables();
            createDefaultAdmin();
            System.out.println("✅ Database initialized successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables() throws SQLException {
        Statement stmt = conn.createStatement();


        String usersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "full_name TEXT NOT NULL," +
                "email TEXT," +
                "phone TEXT," +
                "role TEXT NOT NULL CHECK(role IN ('admin', 'user'))" +
                ")";


        String busesTable = "CREATE TABLE IF NOT EXISTS buses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "bus_number TEXT UNIQUE NOT NULL," +
                "bus_name TEXT NOT NULL," +
                "source TEXT NOT NULL," +
                "destination TEXT NOT NULL," +
                "departure_time TEXT NOT NULL," +
                "arrival_time TEXT NOT NULL," +
                "total_seats INTEGER NOT NULL," +
                "available_seats INTEGER NOT NULL," +
                "fare REAL NOT NULL" +
                ")";

        String ticketsTable = "CREATE TABLE IF NOT EXISTS tickets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "bus_id INTEGER NOT NULL," +
                "booking_date TEXT NOT NULL," +
                "journey_date TEXT NOT NULL," +
                "seats_booked INTEGER NOT NULL," +
                "total_fare REAL NOT NULL," +
                "status TEXT NOT NULL CHECK(status IN ('confirmed', 'cancelled'))," +
                "FOREIGN KEY(user_id) REFERENCES users(id)," +
                "FOREIGN KEY(bus_id) REFERENCES buses(id)" +
                ")";

        stmt.execute(usersTable);
        stmt.execute(busesTable);
        stmt.execute(ticketsTable);
        stmt.close();
    }

    private static void createDefaultAdmin() throws SQLException {
        String checkAdmin = "SELECT COUNT(*) FROM users WHERE role = 'admin'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(checkAdmin);

        if (rs.next() && rs.getInt(1) == 0) {
            String insertAdmin = "INSERT INTO users (username, password, full_name, email, role) " +
                    "VALUES ('admin', 'admin123', 'Administrator', 'admin@busflow.com', 'admin')";
            stmt.execute(insertAdmin);
            System.out.println("✅ Default admin created (username: admin, password: admin123)");
        }
        rs.close();
        stmt.close();
    }

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet authenticateUser(String username, String password) {
        try {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean registerUser(String username, String password, String fullName, String email, String phone) {
        try {
            String query = "INSERT INTO users (username, password, full_name, email, phone, role) VALUES (?, ?, ?, ?, ?, 'user')";
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, fullName);
            pstmt.setString(4, email);
            pstmt.setString(5, phone);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addBus(String busNumber, String busName, String source, String destination,
                                 String departureTime, String arrivalTime, int totalSeats, double fare) {
        try {
            String query = "INSERT INTO buses (bus_number, bus_name, source, destination, departure_time, " +
                    "arrival_time, total_seats, available_seats, fare) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            pstmt.setString(1, busNumber);
            pstmt.setString(2, busName);
            pstmt.setString(3, source);
            pstmt.setString(4, destination);
            pstmt.setString(5, departureTime);
            pstmt.setString(6, arrivalTime);
            pstmt.setInt(7, totalSeats);
            pstmt.setInt(8, totalSeats);
            pstmt.setDouble(9, fare);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateBus(int busId, String busNumber, String busName, String source, String destination,
                                    String departureTime, String arrivalTime, int totalSeats, double fare) {
        try {
            String query = "UPDATE buses SET bus_number = ?, bus_name = ?, source = ?, destination = ?, " +
                    "departure_time = ?, arrival_time = ?, total_seats = ?, fare = ? WHERE id = ?";
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            pstmt.setString(1, busNumber);
            pstmt.setString(2, busName);
            pstmt.setString(3, source);
            pstmt.setString(4, destination);
            pstmt.setString(5, departureTime);
            pstmt.setString(6, arrivalTime);
            pstmt.setInt(7, totalSeats);
            pstmt.setDouble(8, fare);
            pstmt.setInt(9, busId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteBus(int busId) {
        try {
            String query = "DELETE FROM buses WHERE id = ?";
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            pstmt.setInt(1, busId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ResultSet getAllBuses() {
        try {
            String query = "SELECT * FROM buses";
            Statement stmt = getConnection().createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet searchBuses(String source, String destination) {
        try {
            String query = "SELECT * FROM buses WHERE source LIKE ? AND destination LIKE ?";
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            pstmt.setString(1, "%" + source + "%");
            pstmt.setString(2, "%" + destination + "%");
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean bookTicket(int userId, int busId, String journeyDate, int seatsBooked, double totalFare) {
        try {
            Connection c = getConnection();
            c.setAutoCommit(false);

            String checkSeats = "SELECT available_seats FROM buses WHERE id = ?";
            PreparedStatement pstmt1 = c.prepareStatement(checkSeats);
            pstmt1.setInt(1, busId);
            ResultSet rs = pstmt1.executeQuery();

            if (rs.next() && rs.getInt("available_seats") >= seatsBooked) {
                String insertTicket = "INSERT INTO tickets (user_id, bus_id, booking_date, journey_date, " +
                        "seats_booked, total_fare, status) VALUES (?, ?, date('now'), ?, ?, ?, 'confirmed')";
                PreparedStatement pstmt2 = c.prepareStatement(insertTicket);
                pstmt2.setInt(1, userId);
                pstmt2.setInt(2, busId);
                pstmt2.setString(3, journeyDate);
                pstmt2.setInt(4, seatsBooked);
                pstmt2.setDouble(5, totalFare);
                pstmt2.executeUpdate();

                String updateSeats = "UPDATE buses SET available_seats = available_seats - ? WHERE id = ?";
                PreparedStatement pstmt3 = c.prepareStatement(updateSeats);
                pstmt3.setInt(1, seatsBooked);
                pstmt3.setInt(2, busId);
                pstmt3.executeUpdate();

                c.commit();
                c.setAutoCommit(true);
                return true;
            }

            c.rollback();
            c.setAutoCommit(true);
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                getConnection().rollback();
                getConnection().setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    public static boolean updateBookingStatus(int ticketId, String updatedStatus) {
        try {
            String query = "UPDATE tickets SET status = ? WHERE id = ?";
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            pstmt.setString(1, updatedStatus);
            pstmt.setInt(2, ticketId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean cancelTicket(int ticketId) {
        try {
            Connection c = getConnection();
            c.setAutoCommit(false);

            String getTicket = "SELECT bus_id, seats_booked FROM tickets WHERE id = ?";
            PreparedStatement pstmt1 = c.prepareStatement(getTicket);
            pstmt1.setInt(1, ticketId);
            ResultSet rs = pstmt1.executeQuery();

            if (rs.next()) {
                int busId = rs.getInt("bus_id");
                int seatsBooked = rs.getInt("seats_booked");

                String updateTicket = "UPDATE tickets SET status = 'cancelled' WHERE id = ?";
                PreparedStatement pstmt2 = c.prepareStatement(updateTicket);
                pstmt2.setInt(1, ticketId);
                pstmt2.executeUpdate();

                String updateSeats = "UPDATE buses SET available_seats = available_seats + ? WHERE id = ?";
                PreparedStatement pstmt3 = c.prepareStatement(updateSeats);
                pstmt3.setInt(1, seatsBooked);
                pstmt3.setInt(2, busId);
                pstmt3.executeUpdate();

                c.commit();
                c.setAutoCommit(true);
                return true;
            }

            c.rollback();
            c.setAutoCommit(true);
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                getConnection().rollback();
                getConnection().setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    public static ResultSet getUserTickets(int userId) {
        try {
            String query = "SELECT t.*, b.bus_number, b.bus_name, b.source, b.destination, " +
                    "b.departure_time, b.arrival_time FROM tickets t " +
                    "JOIN buses b ON t.bus_id = b.id WHERE t.user_id = ? ORDER BY t.booking_date DESC";
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            pstmt.setInt(1, userId);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet getAllUsers() {
        try {
            String query = "SELECT id, username, full_name, email, phone, role FROM users WHERE role = 'user'";
            Statement stmt = getConnection().createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet getAllTickets() {
        try {
            String query = "SELECT t.*, u.username, u.full_name, b.bus_number, b.bus_name, b.source, b.destination " +
                    "FROM tickets t " +
                    "JOIN users u ON t.user_id = u.id " +
                    "JOIN buses b ON t.bus_id = b.id " +
                    "ORDER BY t.booking_date DESC";
            Statement stmt = getConnection().createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean updateUser(int userId, String fullName, String email, String phone) {
        try {
            String query = "UPDATE users SET full_name = ?, email = ?, phone = ? WHERE id = ?";
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteUser(int userId) {
        try {
            String query = "DELETE FROM users WHERE id = ?";
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getTotalBuses() {
        try {
            String query = "SELECT COUNT(*) FROM buses";
            Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTotalUsers() {
        try {
            String query = "SELECT COUNT(*) FROM users WHERE role = 'user'";
            Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTotalBookings() {
        try {
            String query = "SELECT COUNT(*) FROM tickets WHERE status = 'confirmed'";
            Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getTotalRevenue() {
        try {
            String query = "SELECT SUM(total_fare) FROM tickets WHERE status = 'confirmed'";
            Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static Map<String, Integer> getBookingStatusCount() {
        Map<String, Integer> statusCount = new HashMap<>();
        statusCount.put("confirmed", 0);
        statusCount.put("cancelled", 0);

        try {
            String query = "SELECT status, COUNT(*) as count FROM tickets GROUP BY status";
            Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                statusCount.put(status, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statusCount;
    }

    public static Map<String, Double> getRouteRevenue() {
        Map<String, Double> routeRevenue = new HashMap<>();

        try {
            String query = "SELECT b.source || ' → ' || b.destination as route, " +
                    "SUM(t.total_fare) as revenue " +
                    "FROM tickets t " +
                    "JOIN buses b ON t.bus_id = b.id " +
                    "WHERE t.status = 'confirmed' " +
                    "GROUP BY b.source, b.destination " +
                    "ORDER BY revenue DESC " +
                    "LIMIT 5";

            Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String route = rs.getString("route");
                double revenue = rs.getDouble("revenue");
                routeRevenue.put(route, revenue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return routeRevenue;
    }
}