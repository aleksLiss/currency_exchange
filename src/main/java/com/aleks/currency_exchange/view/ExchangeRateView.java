package com.aleks.currency_exchange.view;

import com.aleks.currency_exchange.model.Currency;

public class ExchangeRateView {

    private int id;
    private Currency baseCurrency;
    private Currency targetCurrency;
    private double rate;

    public ExchangeRateView(int id, Currency baseCurrency, Currency targetCurrency, double rate) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

}
