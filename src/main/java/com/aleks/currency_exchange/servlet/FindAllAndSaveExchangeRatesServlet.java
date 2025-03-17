package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.view.ExceptionView;
import com.aleks.currency_exchange.view.ExchangeRateView;
import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.model.ExchangeRate;
import com.aleks.currency_exchange.repository.SqliteCurrencyRepository;
import com.aleks.currency_exchange.repository.SqliteExchangeRateRepository;
import com.aleks.currency_exchange.service.CurrencyService;
import com.aleks.currency_exchange.service.ExchangeRateService;
import com.aleks.currency_exchange.validator.Validator;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

// http://localhost:8080/currency_exchange/exchangeRates

@WebServlet("/exchangeRates")
public class FindAllAndSaveExchangeRatesServlet extends HttpServlet implements Validator {

    private ExchangeRateService exchangeRateService;
    private CurrencyService currencyService;
    private ExceptionView exceptionView;

    @Override
    public void init(ServletConfig config) {
        currencyService = new CurrencyService(new SqliteCurrencyRepository());
        exchangeRateService = new ExchangeRateService(new SqliteExchangeRateRepository());
        exceptionView = new ExceptionView();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            try {
                Collection<ExchangeRate> exchangeRates = exchangeRateService.findAll();
                if (exchangeRates.isEmpty()) {
                    exceptionView.setMessage("Exchange rates not found");
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                List<ExchangeRateView> exchangeRateViews = new ArrayList<>();
                exchangeRates.forEach(exchangeRate -> {
                    Optional<Currency> foundBaseCurr = currencyService.findById(exchangeRate.getBaseCurrencyId());
                    Optional<Currency> foundTargCurr = currencyService.findById(exchangeRate.getTargetCurrencyId());
                    if (foundBaseCurr.isPresent() && foundTargCurr.isPresent()) {
                        exchangeRateViews.add(
                                new ExchangeRateView(
                                        exchangeRate.getId(),
                                        currencyService.findById(exchangeRate.getBaseCurrencyId()).get(),
                                        currencyService.findById(exchangeRate.getTargetCurrencyId()).get(),
                                        exchangeRate.getRate()
                                )
                        );
                    }
                });
                writer.println(new GsonBuilder().create().toJson(exchangeRateViews));
            } catch (Exception ex) {
                exceptionView.setMessage("Internal error");
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new GsonBuilder().create().toJson(exceptionView));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            try {
                Map<String, String> parameters = getParametersAsMap(req);
                if (!isValidParameters(parameters)) {
                    exceptionView.setMessage("Fields: baseCurrencyCode, targetCurrencyCode, rate must be not empty and must be correct");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                String baseCurrencyCode = parameters.getOrDefault("targetCurrencyCode", null);
                String targetCurrencyCode = parameters.getOrDefault("targetCurrencyCode", null);
                String rate = parameters.getOrDefault("rate", null);
                Optional<Currency> foundBaseCurrency = currencyService.findByCode(baseCurrencyCode.toUpperCase());
                Optional<Currency> foundTargetCurrency = currencyService.findByCode(targetCurrencyCode.toUpperCase());
                if (!foundTargetCurrency.isPresent() || !foundBaseCurrency.isPresent()) {
                    exceptionView.setMessage("Base currency or target currency not found");
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                Optional<ExchangeRate> foundExchangeRate = exchangeRateService.findByCode(
                        foundBaseCurrency.get().getId(),
                        foundTargetCurrency.get().getId());
                if (foundExchangeRate.isPresent()) {
                    exceptionView.setMessage("Input exchange rate already exist");
                    resp.sendError(HttpServletResponse.SC_CONFLICT, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                Optional<ExchangeRate> savedExchangeRate = exchangeRateService.save(
                        new ExchangeRate(
                                foundBaseCurrency.get().getId(),
                                foundTargetCurrency.get().getId(),
                                BigDecimal.valueOf(Double.parseDouble(rate))
                ));
                if (!savedExchangeRate.isPresent()) {
                    exceptionView.setMessage("Error save exchange rate into database");
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                ExchangeRateView exchangeRateView = new ExchangeRateView(
                        savedExchangeRate.get().getId(),
                        foundBaseCurrency.get(),
                        foundTargetCurrency.get(),
                        savedExchangeRate.get().getRate()
                );

                writer.println(new GsonBuilder().create().toJson(exchangeRateView));
            } catch (Exception ex) {
                exceptionView.setMessage("Incorrect fields of parameters");
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new GsonBuilder().create().toJson(exceptionView));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isValidParameters(Map<String, String> parameters) {
        String baseCurrencyCode = parameters.getOrDefault("baseCurrencyCode", null);
        String targetCurrencyCode = parameters.getOrDefault("targetCurrencyCode", null);
        String rate = parameters.getOrDefault("rate", null);
        boolean isValid = true;
        if (baseCurrencyCode == null || baseCurrencyCode.isEmpty() ||
                targetCurrencyCode == null || targetCurrencyCode.isEmpty() ||
                rate == null || rate.isEmpty()) {
            isValid = false;
        }
        return isValid;
    }

    private Map<String, String> getParametersAsMap(HttpServletRequest request) {
        Map<String, String> parameters = new HashMap<>();
        for(Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            parameters.put(entry.getKey(), entry.getValue()[0]);
        }
        return parameters;
    }
}
