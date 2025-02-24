package com.aleks.currency_exchange.service;

import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.repository.CurrencyRepository;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public Collection<Currency> findAll() throws SQLException, ClassNotFoundException {
        return currencyRepository.findAll();
    }

    ;

    public Optional<Currency> findByCode(String code) throws SQLException, ClassNotFoundException {
        return currencyRepository.findByCode(code);
    }

    ;

    public Optional<Currency> save(Currency currency) throws SQLException, ClassNotFoundException {
        return currencyRepository.save(currency);
    }

    ;

    public boolean deleteByCode(String code) throws SQLException, ClassNotFoundException {
        return currencyRepository.deleteByCode(code);
    }

}
