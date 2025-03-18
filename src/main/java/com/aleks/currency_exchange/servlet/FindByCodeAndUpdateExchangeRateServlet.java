package com.aleks.currency_exchange.servlet;

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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//      http://localhost:8080/currency_exchange/exchangeRate/usdeur

@WebServlet("/exchangeRate/*")
public class FindByCodeAndUpdateExchangeRateServlet extends HttpServlet {

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
                String path = req.getPathInfo();
                if (path.isEmpty()) {
                    exceptionView.setMessage("Exchange rate field must contains two code of currencies");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                String[] codesBaseAndTargetCurrencies = getCodesCurrenciesFromPath(path);
                Optional<Currency> baseCurrency = currencyService.findByCode(codesBaseAndTargetCurrencies[0]);
                Optional<Currency> targetCurrency = currencyService.findByCode(codesBaseAndTargetCurrencies[1]);
                if (!baseCurrency.isPresent() || !targetCurrency.isPresent()) {
                    exceptionView.setMessage("Currencies with input codes not found");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                Optional<ExchangeRate> exchangeRate = exchangeRateService.findByCode(baseCurrency.get().getId(), targetCurrency.get().getId());
                if (!exchangeRate.isPresent()) {
                    exceptionView.setMessage("Exchange rate not found");
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                ;
                ExchangeRateView exchangeRateView = new ExchangeRateView(
                        exchangeRate.get().getId(),
                        baseCurrency.get(),
                        targetCurrency.get(),
                        exchangeRate.get().getRate()
                );
                writer.println(new GsonBuilder().create().toJson(exchangeRateView));
            } catch (Exception exception) {
                exceptionView.setMessage("Exchange rate field must contains two code of currencies");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, new GsonBuilder().create().toJson(exceptionView));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String[] getCodesCurrenciesFromPath(String path) {
        String[] arrOfPath = path.split("/");
        String codesOfCurrencies = arrOfPath[1];
        String codeOfBaseCurrency = codesOfCurrencies.substring(0, 3).toUpperCase();
        String codeOfTargetCurrency = codesOfCurrencies.substring(3).toUpperCase();
        return new String[]{codeOfBaseCurrency, codeOfTargetCurrency};
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"PATCH".equalsIgnoreCase(req.getMethod())) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed");
            return;
        }
        try (PrintWriter writer = resp.getWriter()) {
            try {
                String path = req.getPathInfo();
                String[] arrOfPath = path.split("/");
                String rate = getParametersAsMap(req).getOrDefault("rate", null);
                if (arrOfPath.length == 0) {
                    exceptionView.setMessage("Empty codes of currencies");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                String codesOfCurrencies = arrOfPath[1];
                String codeOfBaseCurrency = codesOfCurrencies.substring(0, 3).toUpperCase();
                String codeOfTargetCurrency = codesOfCurrencies.substring(3).toUpperCase();
                Optional<Currency> baseCurrency = currencyService.findByCode(codeOfBaseCurrency);   // empty
                Optional<Currency> targetCurrency = currencyService.findByCode(codeOfTargetCurrency);
                if (!baseCurrency.isPresent() || !targetCurrency.isPresent()) {
                    exceptionView.setMessage("Base or target currency not found");
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                Optional<ExchangeRate> foundExchangeRate = exchangeRateService.findByCode(baseCurrency.get().getId(), targetCurrency.get().getId()); // empty
                if (!foundExchangeRate.isPresent()) {
                    exceptionView.setMessage("Exchange rate not found");
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                if (rate == null || rate.isEmpty()) {
                    exceptionView.setMessage("Empty field rate");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                if (!isNumber(rate)) {
                    exceptionView.setMessage("Input rate must be a number");
                    resp.sendError(HttpServletResponse.SC_CONFLICT, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                if (Double.parseDouble(rate) <= 0) {
                    exceptionView.setMessage("Input rate must be great than zero");
                    resp.sendError(HttpServletResponse.SC_CONFLICT, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                boolean isUpdated = exchangeRateService.update(
                        new ExchangeRate(
                                foundExchangeRate.get().getId(),
                                baseCurrency.get().getId(),
                                targetCurrency.get().getId(),
                                BigDecimal.valueOf(Double.parseDouble(rate))
                        )
                );
                if (!isUpdated) {
                    exceptionView.setMessage("Error update exchange rate");
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                Optional<ExchangeRate> updatedExchangeRate = exchangeRateService.findByCode(baseCurrency.get().getId(), targetCurrency.get().getId());
                ExchangeRateView exchangeRateView = new ExchangeRateView(
                        updatedExchangeRate.get().getId(),
                        baseCurrency.get(),
                        targetCurrency.get(),
                        updatedExchangeRate.get().getRate()
                );
                writer.println(new GsonBuilder().create().toJson(exchangeRateView));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getParametersAsMap(HttpServletRequest request) {
        StringBuilder jsonBuilder = new StringBuilder();
        Map<String, String> parameters = new HashMap<>();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String[] keyVal = jsonBuilder.toString().split("=");
        String key = jsonBuilder.toString().split("=")[0];
        String value = null;
        if (keyVal.length == 2) {
            value = jsonBuilder.toString().split("=")[1];
        }
        parameters.put(key, value);
        return parameters;
    }

    private boolean isNumber(String rate) {
        boolean isNumber = true;
        try {
            BigDecimal.valueOf(Double.parseDouble(rate));
        } catch (Exception ex) {
            isNumber = false;
        }
        return isNumber;
    }

}
