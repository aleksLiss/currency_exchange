package com.aleks.currency_exchange.view;

import com.aleks.currency_exchange.model.Currency;

import java.math.BigDecimal;

public class ExchangeRateView {

    private int id;
    private CurrencyView baseCurrency;
    private CurrencyView targetCurrency;
    private BigDecimal rate;

    public ExchangeRateView(int id, CurrencyView baseCurrency, CurrencyView targetCurrency, BigDecimal rate) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }
}
