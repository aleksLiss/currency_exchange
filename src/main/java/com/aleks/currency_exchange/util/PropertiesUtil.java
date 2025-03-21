package com.aleks.currency_exchange.util;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    private static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    private PropertiesUtil(){}

    private static void loadProperties() {
        try (InputStream inputStream = PropertiesUtil.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            PROPERTIES.load(inputStream);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }
}
