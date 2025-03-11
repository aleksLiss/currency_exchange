package com.aleks.currency_exchange.repository;

import com.aleks.currency_exchange.model.ExchangeRate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SqliteExchangeRateRepository implements ExchangeRateRepository {

    private Connection connection;
    private static final String JDBC_NAME = "org.sqlite.JDBC";
    private static final String URL = "jdbc:sqlite:/home/aleksei/java/projects/currency_exchange/db/currencies.db";

    public SqliteExchangeRateRepository() {
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
    public Collection<ExchangeRate> findAll() {
        initConnection();
        String sql = "SELECT id, base_currency_id, target_currency_id, rate FROM exchange_rates";
        List<ExchangeRate> foundAll = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                foundAll.add(
                        new ExchangeRate(
                                resultSet.getInt("id"),
                                resultSet.getInt("base_currency_id"),
                                resultSet.getInt("target_currency_id"),
                                resultSet.getDouble("rate"))
                );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection();
        }
        return foundAll;
    }

    @Override
    public Optional<ExchangeRate> findByCode(int baseId, int targetId) {
        initConnection();
        String sql = "SELECT id, base_currency_id, target_currency_id, rate FROM exchange_rates WHERE base_currency_id = ? AND target_currency_id = ?";
        Optional<ExchangeRate> foundExchangeRate = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, baseId);
            preparedStatement.setInt(2, targetId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }
            foundExchangeRate = Optional.ofNullable(
                    new ExchangeRate(
                            resultSet.getInt("id"),
                            resultSet.getInt("base_currency_id"),
                            resultSet.getInt("target_currency_id"),
                            resultSet.getDouble("rate")
                    ));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection();
        }
        return foundExchangeRate;
    }

    @Override
    public Optional<ExchangeRate> save(ExchangeRate exchangeRate) {
        initConnection();
        String sql = "INSERT INTO exchange_rates(base_currency_id, target_currency_id, rate) VALUES (?, ?, ?)";
        Optional<ExchangeRate> savedExchangeRate = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, exchangeRate.getBaseCurrencyId());
            preparedStatement.setInt(2, exchangeRate.getTargetCurrencyId());
            preparedStatement.setDouble(3, exchangeRate.getRate());
            preparedStatement.execute();
            try (ResultSet generatedKey = preparedStatement.getGeneratedKeys()) {
                if (generatedKey.next()) {
                    exchangeRate.setId(generatedKey.getInt(1));
                    savedExchangeRate = Optional.ofNullable(
                            exchangeRate
                    );
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection();
        }
        return savedExchangeRate;
    }

    @Override
    public boolean update(ExchangeRate exchangeRate) {
        initConnection();
        String sql = "UPDATE exchange_rates SET base_currency_id = ?, target_currency_id = ?, rate = ? WHERE id = ?";
        boolean isUpdated = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, exchangeRate.getBaseCurrencyId());
            preparedStatement.setInt(2, exchangeRate.getTargetCurrencyId());
            preparedStatement.setDouble(3, exchangeRate.getRate());
            preparedStatement.setInt(4, exchangeRate.getId());
            isUpdated = preparedStatement.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection();
        }
        return isUpdated;
    }
}
