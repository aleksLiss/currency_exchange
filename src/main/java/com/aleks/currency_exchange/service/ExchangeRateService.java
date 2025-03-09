package com.aleks.currency_exchange.service;

import com.aleks.currency_exchange.model.ExchangeRate;
import com.aleks.currency_exchange.repository.ExchangeRateRepository;

import java.util.Collection;
import java.util.Optional;

public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public Collection<ExchangeRate> findAll() {
        return exchangeRateRepository.findAll();
    }

    ;

    public Optional<ExchangeRate> findByCode(int baseId, int targetId) {
        return exchangeRateRepository.findByCode(baseId, targetId);
    }

    ;

    public Optional<ExchangeRate> save(ExchangeRate exchangeRate) {
        return exchangeRateRepository.save(exchangeRate);
    }

    ;

    public boolean update(ExchangeRate exchangeRate) {
        return exchangeRateRepository.update(exchangeRate);
    }

    ;
}
