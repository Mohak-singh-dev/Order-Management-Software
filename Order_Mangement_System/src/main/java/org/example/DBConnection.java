package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Creates connections to the ecommerce_db MySQL database. */
public final class DBConnection {
    private static final String URL = env("DB_URL", "jdbc:mysql://localhost:3306/ecommerce_db");
    private static final String USER = "root";
    private static final String PASSWORD ="Mohak@501";

    private DBConnection() {
        // Utility class; do not instantiate.
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
