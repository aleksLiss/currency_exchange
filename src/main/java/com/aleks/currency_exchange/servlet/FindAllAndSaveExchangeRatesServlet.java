package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.view.ExchangeRateView;
import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.model.ExchangeRate;
import com.aleks.currency_exchange.repository.CurrencyRepository;
import com.aleks.currency_exchange.repository.ExchangeRateRepository;
import com.aleks.currency_exchange.repository.SqliteCurrencyRepository;
import com.aleks.currency_exchange.repository.SqliteExchangeRateRepository;
import com.aleks.currency_exchange.service.CurrencyService;
import com.aleks.currency_exchange.service.ExchangeRateService;
import com.aleks.currency_exchange.templater.Templater;
import com.aleks.currency_exchange.validator.Validator;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.util.*;

// http://localhost:8080/currency_exchange/exchangeRates

@WebServlet("/exchangeRates")
public class FindAllAndSaveExchangeRatesServlet extends HttpServlet implements Templater, Validator {

    private ExchangeRateService exchangeRateService;
    private ExchangeRateRepository exchangeRateRepository;
    private CurrencyService currencyService;
    private CurrencyRepository currencyRepository;

    @Override
    public void init(ServletConfig config) {
        currencyRepository = new SqliteCurrencyRepository();
        currencyService = new CurrencyService(currencyRepository);
        exchangeRateRepository = new SqliteExchangeRateRepository();
        exchangeRateService = new ExchangeRateService(exchangeRateRepository);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            resp.setContentType("text/html;encoding=utf-8");
            try {
                Collection<ExchangeRate> exchangeRates = exchangeRateService.findAll();
                if (exchangeRates.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Exchange rates not found");
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
                writer.println(getTemplate(new GsonBuilder().create().toJson(exchangeRateViews)));
            } catch (Exception ex) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            try {
                resp.setContentType("text/html;encoding=utf-8");
                Map<String, String> parameters = getParametersAsMap(req);
                if (!isValidParameters(parameters)) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Fields: 'baseCurrencyCode', 'targetCurrencyCode', 'rate' must be not empty and must be correct");
                    return;
                }
                String baseCurrencyCode = parameters.getOrDefault("targetCurrencyCode", null);
                String targetCurrencyCode = parameters.getOrDefault("targetCurrencyCode", null);
                String rate = parameters.getOrDefault("rate", null);
                Optional<Currency> foundBaseCurrency = currencyService.findByCode(baseCurrencyCode.toUpperCase());
                Optional<Currency> foundTargetCurrency = currencyService.findByCode(targetCurrencyCode.toUpperCase());
                if (!foundTargetCurrency.isPresent() || !foundBaseCurrency.isPresent()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Base currency or target currency not found");
                    return;
                }
                Optional<ExchangeRate> foundExchangeRate = exchangeRateService.findByCode(
                        foundBaseCurrency.get().getId(),
                        foundTargetCurrency.get().getId());
                if (foundExchangeRate.isPresent()) {
                    resp.sendError(HttpServletResponse.SC_CONFLICT, "Input exchange rate already exist");
                    return;
                }
                Optional<ExchangeRate> savedExchangeRate = exchangeRateService.save(
                        new ExchangeRate(
                                foundBaseCurrency.get().getId(),
                                foundTargetCurrency.get().getId(),
                                Double.parseDouble(rate))
                );
                if (!savedExchangeRate.isPresent()) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error save exchange rate into database");
                    return;
                }
                ExchangeRateView exchangeRateView = new ExchangeRateView(
                        savedExchangeRate.get().getId(),
                        foundBaseCurrency.get(),
                        foundTargetCurrency.get(),
                        savedExchangeRate.get().getRate()
                );
                writer.println(getTemplate(new GsonBuilder().create().toJson(exchangeRateView)));
            } catch (Exception ex) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Incorrect fields of parameters");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getTemplate(String content) {
        return "<html><body>" +
                content +
                "</body></html>";
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
