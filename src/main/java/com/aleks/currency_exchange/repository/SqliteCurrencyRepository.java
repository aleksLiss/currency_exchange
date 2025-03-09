package com.aleks.currency_exchange.repository;

import com.aleks.currency_exchange.model.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SqliteCurrencyRepository implements CurrencyRepository {
    private Connection connection;
    private static final String JDBC_NAME = "org.sqlite.JDBC";
    private static final String URL = "jdbc:sqlite:db/currencies.db";

    public SqliteCurrencyRepository() {
        initConnection();
    }

    private void initConnection() {
        try {
            Class.forName(JDBC_NAME);
            connection = DriverManager.getConnection(URL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Collection<Currency> findAll() {
        initConnection();
        List<Currency> currencies = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, code, full_name, sign FROM currencies")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    currencies.add(new Currency(
                            resultSet.getInt("id"),
                            resultSet.getString("code"),
                            resultSet.getString("full_name"),
                            resultSet.getString("sign")
                    ));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection();
        }
        return currencies;
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        initConnection();
        Optional<Currency> currency = Optional.empty();
        String sql = "SELECT id, code, full_name, sign FROM currencies WHERE code = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                currency = Optional.ofNullable(new Currency(
                        resultSet.getInt("id"),
                        resultSet.getString("code"),
                        resultSet.getString("full_name"),
                        resultSet.getString("sign")
                ));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection();
        }
        return currency;
    }

    @Override
    public Optional<Currency> save(Currency currency) {
        initConnection();
        String sql = "INSERT INTO currencies(code, full_name, sign) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.execute();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    currency.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection();
        }
        return Optional.ofNullable(currency);
    }
}
