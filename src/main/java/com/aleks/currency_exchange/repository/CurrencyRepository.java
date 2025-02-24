package com.aleks.currency_exchange.repository;

import com.aleks.currency_exchange.model.Currency;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

public interface CurrencyRepository {

    Collection<Currency> findAll() throws SQLException, ClassNotFoundException;

    Optional<Currency> findByCode(String code) throws SQLException, ClassNotFoundException;

    Optional<Currency> save(Currency currency) throws SQLException, ClassNotFoundException;

    boolean deleteByCode(String code) throws SQLException, ClassNotFoundException;

}
