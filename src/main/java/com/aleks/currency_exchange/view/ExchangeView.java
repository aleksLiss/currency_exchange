package com.aleks.currency_exchange.view;


import java.math.BigDecimal;

public class ExchangeView {

    private CurrencyView baseCurrency;
    private CurrencyView targetCurrency;
    private BigDecimal rate;
    private double amount;
    private BigDecimal convertedAmount;

    public ExchangeView(CurrencyView baseCurrency, CurrencyView targetCurrency, BigDecimal rate, double amount, BigDecimal convertedAmount) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
    }
}
