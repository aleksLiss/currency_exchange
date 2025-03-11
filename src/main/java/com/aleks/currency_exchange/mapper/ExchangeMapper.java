package com.aleks.currency_exchange.mapper;

import com.aleks.currency_exchange.model.Currency;

import java.math.BigDecimal;

public class ExchangeMapper {

    private Currency baseCurrency;
    private Currency targetCurrency;
    private double rate;
    private double amount;
    private BigDecimal convertedAmount;

    public ExchangeMapper(Currency baseCurrency, Currency targetCurrency, double rate, double amount, BigDecimal convertedAmount) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
    }
}
