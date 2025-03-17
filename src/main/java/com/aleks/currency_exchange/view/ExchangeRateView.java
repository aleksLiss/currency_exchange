package com.aleks.currency_exchange.view;

import com.aleks.currency_exchange.model.Currency;

import java.math.BigDecimal;

public class ExchangeRateView {

    private int id;
    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;

    public ExchangeRateView(int id, Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }
}
