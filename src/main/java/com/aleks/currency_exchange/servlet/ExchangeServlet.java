package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.validator.NumberParametersValidator;
import com.aleks.currency_exchange.validator.ParametersValidator;
import com.aleks.currency_exchange.view.ExceptionView;
import com.aleks.currency_exchange.view.ExchangeView;
import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.model.ExchangeRate;
import com.aleks.currency_exchange.repository.SqliteCurrencyRepository;
import com.aleks.currency_exchange.repository.SqliteExchangeRateRepository;
import com.aleks.currency_exchange.service.CurrencyService;
import com.aleks.currency_exchange.service.ExchangeRateService;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet implements ParametersValidator, NumberParametersValidator {

    private ExchangeRateService exchangeRateService;
    private CurrencyService currencyService;
    private ExceptionView exceptionView;


    @Override
    public void init(ServletConfig config) {
        exchangeRateService = new ExchangeRateService(new SqliteExchangeRateRepository());
        currencyService = new CurrencyService(new SqliteCurrencyRepository());
        exceptionView = new ExceptionView();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            try {
                Map<String, String> parameters = getParametersAsMap(req.getParameterMap());
                if (!isValidParameters(parameters)) {
                    exceptionView.setMessage("Fields: from, to, amount must be not empty and must be correct");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            new GsonBuilder().create().toJson(exceptionView)
                    );
                    return;
                }
                if (!isNumber(parameters.getOrDefault("amount", null))) {
                    exceptionView.setMessage("Parameter amount must be a number");
                    resp.sendError(HttpServletResponse.SC_CONFLICT, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                double amount = Double.parseDouble(parameters.getOrDefault("amount", String.valueOf(0.0)));
                if (amount < 0) {
                    exceptionView.setMessage("Parameter amount must be great than zero");
                    resp.sendError(HttpServletResponse.SC_CONFLICT, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                Optional<Currency> baseCurrency = currencyService.findByCode(
                        parameters.get("from").toUpperCase());
                Optional<Currency> targetCurrency = currencyService.findByCode(
                        parameters.get("to").toUpperCase());
                if (!baseCurrency.isPresent() || !targetCurrency.isPresent()) {
                    exceptionView.setMessage("Currencies with input codes not found");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                Optional<ExchangeRate> dirExRate = exchangeRateService.findByCode(baseCurrency.get().getId(), targetCurrency.get().getId());
                if (dirExRate.isPresent()) {
                    ExchangeView exchangeView = new ExchangeView(
                            baseCurrency.get(),
                            targetCurrency.get(),
                            dirExRate.get().getRate(),
                            amount,
                            dirExRate.get().getRate().multiply(BigDecimal.valueOf(amount))
                    );
                    writer.println(new GsonBuilder().create().toJson(exchangeView));
                    return;
                }
                Optional<ExchangeRate> backExRate = exchangeRateService.findByCode(targetCurrency.get().getId(), baseCurrency.get().getId());
                if (backExRate.isPresent()) {
                    ExchangeView exchangeView = new ExchangeView(
                            targetCurrency.get(),
                            baseCurrency.get(),
                            backExRate.get().getRate(),
                            amount,
                            backExRate.get().getRate().multiply(BigDecimal.valueOf(amount))
                    );
                    writer.println(new GsonBuilder().create().toJson(exchangeView));
                    return;
                }
                ExchangeRate[] exchangeRates = getTargetsCurrenciesId(baseCurrency.get().getId(), targetCurrency.get().getId());
                if (exchangeRates.length == 2) {
                    BigDecimal rate = exchangeRates[1].getRate().divide(exchangeRates[0].getRate(), 2, RoundingMode.HALF_EVEN);
                    ExchangeView exchangeView = new ExchangeView(
                            baseCurrency.get(),
                            targetCurrency.get(),
                            rate,
                            amount,
                            rate.multiply(BigDecimal.valueOf(amount))
                    );
                    writer.println(new GsonBuilder().create().toJson(exchangeView));
                    return;
                }
                exceptionView.setMessage("Database not contains needed exchange rate");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, new GsonBuilder().create().toJson(exceptionView));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private ExchangeRate[] getTargetsCurrenciesId(int baseCurrId, int targetCurrId) {
        ExchangeRate[] result = new ExchangeRate[2];
        exchangeRateService.findAll()
                .forEach(exRate -> {
                    if (exRate.getBaseCurrencyId() == baseCurrId || exRate.getTargetCurrencyId() == baseCurrId) {
                        if (result[0] == null) {
                            result[0] = exRate;
                        }
                    }
                    ;
                    if (exRate.getTargetCurrencyId() == targetCurrId || exRate.getBaseCurrencyId() == targetCurrId) {
                        if (result[1] == null) {
                            result[1] = exRate;
                        }
                    }
                });
        return result;
    }

    @Override
    public boolean isValidParameters(Map<String, String> parameters) {
        boolean isValid = true;
        if (parameters.isEmpty()) {
            isValid = false;
        } else {
            String baseCodeCurr = parameters.getOrDefault("from", null);
            String targetCodeCurr = parameters.getOrDefault("to", null);
            String amount = parameters.getOrDefault("amount", null);
            if (baseCodeCurr == null || baseCodeCurr.isEmpty() ||
                    targetCodeCurr == null || targetCodeCurr.isEmpty() ||
                    amount == null || amount.isEmpty()) {
                isValid = false;
            }
        }
        return isValid;
    }

    private Map<String, String> getParametersAsMap(Map<String, String[]> parametersMap) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parametersMap.entrySet()) {
            if (entry.getValue().length == 1) {
                result.put(entry.getKey(), entry.getValue()[0]);
            }
        }
        return result;
    }

    @Override
    public boolean isNumber(String number) {
        boolean isNumber = false;
        try {
            BigDecimal.valueOf(Double.parseDouble(number));
            isNumber = true;
        } catch (NumberFormatException e) {
        }
        return isNumber;
    }
}

