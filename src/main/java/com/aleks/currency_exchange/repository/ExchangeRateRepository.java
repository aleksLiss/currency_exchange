package com.aleks.currency_exchange.repository;

import com.aleks.currency_exchange.model.ExchangeRate;

import java.util.Collection;
import java.util.Optional;

public interface ExchangeRateRepository {

    Collection<ExchangeRate> findAll();

    Optional<ExchangeRate> findByCode(int baseId, int targetId);

    Optional<ExchangeRate> save(ExchangeRate exchangeRate);

    boolean update(ExchangeRate exchangeRate);
}
