package com.aleks.currency_exchange.view;

import com.aleks.currency_exchange.exception.ExchangeException;

public class ExceptionView {

    private String message;

    public ExceptionView(String message) {
        this.message = message;
    }

    public ExceptionView() {
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
