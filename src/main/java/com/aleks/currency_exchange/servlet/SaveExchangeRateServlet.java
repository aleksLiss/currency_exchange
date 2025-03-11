package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.model.ExchangeRate;
import com.aleks.currency_exchange.repository.CurrencyRepository;
import com.aleks.currency_exchange.repository.ExchangeRateRepository;
import com.aleks.currency_exchange.repository.SqliteCurrencyRepository;
import com.aleks.currency_exchange.repository.SqliteExchangeRateRepository;
import com.aleks.currency_exchange.service.CurrencyService;
import com.aleks.currency_exchange.service.ExchangeRateService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

@WebServlet("/exchangeRates/*")
public class SaveExchangeRateServlet extends HttpServlet {

    private ExchangeRateRepository exchangeRateRepository;
    private ExchangeRateService exchangeRateService;
    private CurrencyRepository currencyRepository;
    private CurrencyService currencyService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        exchangeRateRepository = new SqliteExchangeRateRepository();
        exchangeRateService = new ExchangeRateService(exchangeRateRepository);
        currencyRepository = new SqliteCurrencyRepository();
        currencyService = new CurrencyService(currencyRepository);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    // http://localhost:8080/currency_exchange/exchangeRates/?baseCurrencyCode=????&targetCurrencyCode=??&rate=??



}
