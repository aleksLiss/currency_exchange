package com.aleks.currency_exchange.repository;

import com.aleks.currency_exchange.connection.ConnectionManager;
import com.aleks.currency_exchange.model.Currency;
import java.sql.*;
import java.util.*;

public class SqliteCurrencyRepository implements CurrencyRepository {

    public SqliteCurrencyRepository() {
    }


    @Override
    public Collection<Currency> findAll() {
        List<Currency> currencies = new ArrayList<>();
        String sql = "SELECT id, code, full_name, sign FROM currencies";
        try (Connection connection = ConnectionManager.openConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
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
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return currencies;
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        Optional<Currency> currency = Optional.empty();
        String sql = "SELECT id, code, full_name, sign FROM currencies WHERE code = ?";
        try (Connection connection = ConnectionManager.openConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, code.toUpperCase());
                try {
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
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return currency;
    }

    @Override
    public Optional<Currency> save(Currency currency) {
        String sql = "INSERT INTO currencies(code, full_name, sign) VALUES (?, ?, ?)";
        try (Connection connection = ConnectionManager.openConnection()) {
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
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.ofNullable(currency);
    }

    @Override
    public Optional<Currency> findById(int id) {
        Optional<Currency> foundCurrency = Optional.empty();
        String sql = "SELECT id, code, full_name, sign FROM currencies WHERE id = ?";
        try (Connection connection = ConnectionManager.openConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, id);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        foundCurrency = Optional.ofNullable(
                                new Currency(
                                        resultSet.getInt("id"),
                                        resultSet.getString("code"),
                                        resultSet.getString("full_name"),
                                        resultSet.getString("sign")
                                )
                        );
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return foundCurrency;
    }
}
