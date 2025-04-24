package db;

import config.AppConfig;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

public class ConnectionManager {

    static {
        try {
            Class.forName(AppConfig.get("jdbc.driver"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver load failed", e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    AppConfig.get("jdbc.url"),
                    AppConfig.get("jdbc.username"),
                    AppConfig.get("jdbc.password")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
