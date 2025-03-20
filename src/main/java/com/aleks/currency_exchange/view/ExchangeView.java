package com.aleks.currency_exchange.view;

import com.aleks.currency_exchange.model.Currency;

import java.math.BigDecimal;

public class ExchangeView {

    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;
    private double amount;
    private BigDecimal convertedAmount;

    public ExchangeView(Currency baseCurrency, Currency targetCurrency, BigDecimal rate, double amount, BigDecimal convertedAmount) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
    }
}
