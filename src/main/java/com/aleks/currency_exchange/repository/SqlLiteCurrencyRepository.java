package com.aleks.currency_exchange.repository;

import com.aleks.currency_exchange.model.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SqlLiteCurrencyRepository implements CurrencyRepository {
    private Connection connection;

    private void initConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:/home/aleksei/java/projects/currency_exchange/db/currency_db";
            connection = DriverManager.getConnection(url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Collection<Currency> findAll() {
        initConnection();
        List<Currency> currencies = new ArrayList<>();
        String sql = "SELECT * FROM currencies";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    currencies.add(new Currency(
                            resultSet.getString("code"),
                            resultSet.getString("full_name"),
                            resultSet.getString("sign")
                    ));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return currencies;
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        initConnection();
        Currency currency = null;
        String sql = "SELECT * FROM currencies WHERE code = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new IllegalArgumentException("Currency with id not found!");
            }
            currency = new Currency(
                    resultSet.getString("code"),
                    resultSet.getString("full_name"),
                    resultSet.getString("sign")
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.ofNullable(currency);
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(currency);
    }

    @Override
    public boolean deleteByCode(String code) {
        initConnection();
        boolean isDeleted = false;
        String sql = "DELETE FROM currencies WHERE code = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, code);
            isDeleted = preparedStatement.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isDeleted;
    }
}
