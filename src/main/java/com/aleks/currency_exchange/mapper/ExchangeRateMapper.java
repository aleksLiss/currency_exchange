package com.aleks.currency_exchange.mapper;

import com.aleks.currency_exchange.model.Currency;

public class ExchangeRateMapper {

    private int id;
    private Currency baseCurrency;
    private Currency targetCurrency;
    private double rate;

    public ExchangeRateMapper(int id, Currency baseCurrency, Currency targetCurrency, double rate) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

}
