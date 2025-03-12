package com.aleks.currency_exchange.repository;

import com.aleks.currency_exchange.model.Currency;

import java.util.Collection;
import java.util.Optional;

public interface CurrencyRepository {

    Collection<Currency> findAll();

    Optional<Currency> findByCode(String code);

    Optional<Currency> save(Currency currency);

    Optional<Currency> findById(int id);
}
