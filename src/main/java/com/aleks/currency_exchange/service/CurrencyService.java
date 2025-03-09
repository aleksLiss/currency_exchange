package com.aleks.currency_exchange.service;

import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.repository.CurrencyRepository;

import java.util.Collection;
import java.util.Optional;

public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public Collection<Currency> findAll() {
        return currencyRepository.findAll();
    }

    ;

    public Optional<Currency> findByCode(String code) {
        return currencyRepository.findByCode(code);
    }

    ;

    public Optional<Currency> save(Currency currency) {
        return currencyRepository.save(currency);
    }

    ;


}
