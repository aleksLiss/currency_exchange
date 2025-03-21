package com.aleks.currency_exchange.connection;

import com.aleks.currency_exchange.util.PropertiesUtil;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionManager {

    private static final String URL_KEY = "database.url";
    private static final String JDBC_NAME_KEY = "jdbc.name";

    private ConnectionManager() {
    }

    static {
        loadDriver();
    }

    private static void loadDriver() {
        try {
            Class.forName(
                    PropertiesUtil.get(JDBC_NAME_KEY)
            );
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Connection openConnection() {
        try {
            return DriverManager.getConnection(
                    PropertiesUtil.get(URL_KEY)
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
