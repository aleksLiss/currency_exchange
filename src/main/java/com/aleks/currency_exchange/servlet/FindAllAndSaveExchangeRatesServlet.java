package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.validator.ParametersValidator;
import com.aleks.currency_exchange.validator.NumberParametersValidator;
import com.aleks.currency_exchange.view.CurrencyView;
import com.aleks.currency_exchange.view.ExceptionView;
import com.aleks.currency_exchange.view.ExchangeRateView;
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
import java.util.*;

// http://localhost:8080/currency_exchange/exchangeRates

@WebServlet("/exchangeRates")
public class FindAllAndSaveExchangeRatesServlet extends HttpServlet implements ParametersValidator, NumberParametersValidator {

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
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writer.println(new GsonBuilder().create().toJson(exceptionView));
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
                                        new CurrencyView(
                                                foundBaseCurr.get().getId(),
                                                foundBaseCurr.get().getFullName(),
                                                foundBaseCurr.get().getCode(),
                                                foundBaseCurr.get().getSign()
                                        ),
                                        new CurrencyView(
                                                foundTargCurr.get().getId(),
                                                foundTargCurr.get().getFullName(),
                                                foundTargCurr.get().getCode(),
                                                foundTargCurr.get().getSign()
                                        ),
                                        exchangeRate.getRate()
                                )
                        );
                    }
                });
                writer.println(new GsonBuilder().create().toJson(exchangeRateViews));
            } catch (Exception ex) {
                exceptionView.setMessage("Internal error");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.println(new GsonBuilder().create().toJson(exceptionView));
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
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writer.println(new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                String baseCurrencyCode = parameters.getOrDefault("baseCurrencyCode", null);
                String targetCurrencyCode = parameters.getOrDefault("targetCurrencyCode", null);
                String rate = parameters.getOrDefault("rate", null);
                Optional<Currency> foundBaseCurrency = currencyService.findByCode(baseCurrencyCode.toUpperCase());
                Optional<Currency> foundTargetCurrency = currencyService.findByCode(targetCurrencyCode.toUpperCase());
                if (!foundTargetCurrency.isPresent() || !foundBaseCurrency.isPresent()) {
                    exceptionView.setMessage("Base currency or target currency not found");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writer.println(new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                Optional<ExchangeRate> foundExchangeRate = exchangeRateService.findByCode(
                        foundBaseCurrency.get().getId(),
                        foundTargetCurrency.get().getId());
                if (foundExchangeRate.isPresent()) {
                    exceptionView.setMessage("Input exchange rate already exist");
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    writer.println(new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                if (!isNumber(rate)) {
                    exceptionView.setMessage("Input rate must be a number");
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    writer.println(new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                if (Double.parseDouble(rate) <= 0) {
                    exceptionView.setMessage("Input rate must be great than zero");
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    writer.println(new GsonBuilder().create().toJson(exceptionView));
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
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    writer.println(new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                ExchangeRateView exchangeRateView = new ExchangeRateView(
                        savedExchangeRate.get().getId(),
                        new CurrencyView(
                            foundBaseCurrency.get().getId(),
                            foundBaseCurrency.get().getFullName(),
                            foundBaseCurrency.get().getCode(),
                            foundBaseCurrency.get().getSign()
                        ),
                        new CurrencyView(
                                foundTargetCurrency.get().getId(),
                                foundTargetCurrency.get().getFullName(),
                                foundTargetCurrency.get().getCode(),
                                foundTargetCurrency.get().getSign()
                        ),
                        savedExchangeRate.get().getRate()
                );
                resp.setStatus(HttpServletResponse.SC_CREATED);
                writer.println(new GsonBuilder().create().toJson(exchangeRateView));
            } catch (Exception ex) {
                exceptionView.setMessage("Incorrect fields of parameters");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.println(new GsonBuilder().create().toJson(exceptionView));
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
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            parameters.put(entry.getKey(), entry.getValue()[0]);
        }
        return parameters;
    }

    @Override
    public boolean isNumber(String number) {
        boolean isNumber = true;
        try {
            BigDecimal.valueOf(Double.parseDouble(number));
        } catch (Exception ex) {
            isNumber = false;
        }
        return isNumber;
    }
}
