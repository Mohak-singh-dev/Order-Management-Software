package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Menu-driven JDBC application for the ecommerce_db Orders table.
 * Configure DB_URL, DB_USER, and DB_PASSWORD in the environment before running.
 */
public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in);
             Connection connection = DBConnection.getConnection()) {
            System.out.println("Connected to ecommerce_db.");
            runMenu(scanner, connection);
        } catch (SQLException e) {
            System.err.println("Could not connect to MySQL: " + e.getMessage());
            System.err.println("Check that MySQL is running, ecommerce_db exists, and DB_URL/DB_USER/DB_PASSWORD are correct.");
        }
    }

    private static void runMenu(Scanner scanner, Connection connection) {
        while (true) {
            System.out.println("\n=== Order Management System ===");
            System.out.println("1. Insert three sample orders");
            System.out.println("2. Find orders by status and quantity > 2");
            System.out.println("3. Increase order quantity");
            System.out.println("4. Update order status");
            System.out.println("5. Display total order value");
            System.out.println("6. Delete a cancelled order");
            System.out.println("0. Exit");
            int choice = readInt(scanner, "Choose an option: ");

            try {
                switch (choice) {
                    case 1 -> insertSampleOrders(connection);
                    case 2 -> findOrdersByStatus(scanner, connection);
                    case 3 -> increaseQuantity(scanner, connection);
                    case 4 -> updateStatus(scanner, connection);
                    case 5 -> displayTotalOrderValue(connection);
                    case 6 -> deleteCancelledOrder(scanner, connection);
                    case 0 -> {
                        System.out.println("Goodbye.");
                        return;
                    }
                    default -> System.out.println("Please choose an option from 0 to 6.");
                }
            } catch (SQLException e) {
                System.err.println("Database operation failed: " + e.getMessage());
            }
        }
    }

    /** Inserts three starter rows; duplicate primary keys are reported and skipped. */
    private static void insertSampleOrders(Connection connection) throws SQLException {
        String sql = "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Object[][] orders = {
                {1001, "Aarav Sharma", "Wireless Mouse", 3, 799.00, "Pending"},
                {1002, "Diya Patel", "Mechanical Keyboard", 1, 2499.00, "Shipped"},
                {1003, "Kabir Singh", "USB-C Hub", 4, 1599.50, "Cancelled"}
        };

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Object[] order : orders) {
                statement.setInt(1, (int) order[0]);
                statement.setString(2, (String) order[1]);
                statement.setString(3, (String) order[2]);
                statement.setInt(4, (int) order[3]);
                statement.setDouble(5, (double) order[4]);
                statement.setString(6, (String) order[5]);
                try {
                    statement.executeUpdate();
                    System.out.println("Inserted order " + order[0] + ".");
                } catch (SQLException e) {
                    if (isDuplicateKey(e)) {
                        System.out.println("Order ID " + order[0] + " already exists; skipped.");
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    private static void findOrdersByStatus(Scanner scanner, Connection connection) throws SQLException {
        System.out.print("Enter order status: ");
        String status = scanner.nextLine().trim();
        String sql = "SELECT order_id, customer_name, product_name, quantity, unit_price, status "
                + "FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.isBeforeFirst()) {
                    System.out.println("No matching orders found.");
                    return;
                }
                printOrders(results);
            }
        }
    }

    private static void increaseQuantity(Scanner scanner, Connection connection) throws SQLException {
        int orderId = readInt(scanner, "Enter order ID: ");
        int additionalQuantity = readInt(scanner, "Enter additional quantity: ");
        if (additionalQuantity <= 0) {
            System.out.println("Additional quantity must be greater than zero.");
            return;
        }
        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, additionalQuantity);
            statement.setInt(2, orderId);
            if (statement.executeUpdate() == 0) {
                System.out.println("Order " + orderId + " does not exist.");
            } else {
                System.out.println("Quantity increased for order " + orderId + ".");
            }
        }
    }

    private static void updateStatus(Scanner scanner, Connection connection) throws SQLException {
        int orderId = readInt(scanner, "Enter order ID: ");
        System.out.print("Enter new status (Pending, Shipped, Delivered): ");
        String status = scanner.nextLine().trim();
        if (!isValidStatus(status)) {
            System.out.println("Invalid status. Use Pending, Shipped, or Delivered.");
            return;
        }

        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, orderId);
            if (statement.executeUpdate() == 0) {
                System.out.println("Order " + orderId + " does not exist.");
                return;
            }
        }
        System.out.println("Order updated:");
        displayOrder(connection, orderId);
    }

    private static void displayTotalOrderValue(Connection connection) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * unit_price), 0) AS total_value FROM Orders";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                System.out.printf("Total order value: %.2f%n", result.getDouble("total_value"));
            }
        }
    }

    private static void deleteCancelledOrder(Scanner scanner, Connection connection) throws SQLException {
        int orderId = readInt(scanner, "Enter order ID to delete: ");
        String sql = "DELETE FROM Orders WHERE order_id = ? AND status = 'Cancelled'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            if (statement.executeUpdate() == 1) {
                System.out.println("Cancelled order " + orderId + " deleted.");
            } else {
                System.out.println("Order cannot be deleted: it does not exist or its status is not Cancelled.");
            }
        }
    }

    private static void displayOrder(Connection connection, int orderId) throws SQLException {
        String sql = "SELECT order_id, customer_name, product_name, quantity, unit_price, status "
                + "FROM Orders WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    printOrder(result);
                }
            }
        }
    }

    private static void printOrders(ResultSet results) throws SQLException {
        while (results.next()) {
            printOrder(results);
        }
    }

    private static void printOrder(ResultSet result) throws SQLException {
        System.out.printf("ID: %d | Customer: %s | Product: %s | Quantity: %d | Unit price: %.2f | Status: %s%n",
                result.getInt("order_id"), result.getString("customer_name"), result.getString("product_name"),
                result.getInt("quantity"), result.getDouble("unit_price"), result.getString("status"));
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Enter a whole number.");
            }
        }
    }

    private static boolean isValidStatus(String status) {
        return status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Shipped")
                || status.equalsIgnoreCase("Delivered");
    }

    private static boolean isDuplicateKey(SQLException exception) {
        for (SQLException current = exception; current != null; current = current.getNextException()) {
            if (current.getErrorCode() == 1062 || "23000".equals(current.getSQLState())) {
                return true;
            }
        }
        return false;
    }

}
