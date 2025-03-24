package com.aleks.currency_exchange.connection;

import com.aleks.currency_exchange.util.PropertiesUtil;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

public class ConnectionManager {

    private static final String URL_KEY = "database.url";
    private static final String JDBC_NAME_KEY = "jdbc.name";
    private static final String DATABASE_NAME_KEY = "database.name";

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

    private static Optional<String> getPathToDatabase() {
        Optional<String> result = Optional.empty();
        try {
            URL resource = ConnectionManager.class
                    .getClassLoader()
                    .getResource(
                            PropertiesUtil.get(
                                    DATABASE_NAME_KEY
                            )
                    );
            String path = new File(resource.toURI()).getAbsolutePath();
            String res = PropertiesUtil.get(
                    URL_KEY
            );
            result = Optional.of(res + path);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static Connection openConnection() {
        try {
            Optional<String> absolutePathToDb = getPathToDatabase();
            if (!absolutePathToDb.isPresent()) {
                throw new RuntimeException("Internal error");
            }
            return DriverManager.getConnection(
                    absolutePathToDb.get()
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
