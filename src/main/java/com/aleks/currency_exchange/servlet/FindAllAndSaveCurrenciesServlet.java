package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.repository.SqliteCurrencyRepository;
import com.aleks.currency_exchange.service.CurrencyService;
import com.aleks.currency_exchange.validator.ParametersValidator;
import com.aleks.currency_exchange.view.CurrencyView;
import com.aleks.currency_exchange.view.ExceptionView;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

// http://localhost:8080/currency_exchange/currencies

@WebServlet("/currencies")
public class FindAllAndSaveCurrenciesServlet extends HttpServlet implements ParametersValidator {

    private CurrencyService currencyService;
    private ExceptionView exceptionView;
    @Override
    public void init(ServletConfig config) {
        currencyService = new CurrencyService(new SqliteCurrencyRepository());
        exceptionView = new ExceptionView();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            try {
                Collection<Currency> currencies = currencyService.findAll();
                if (currencies.isEmpty()) {
                    exceptionView.setMessage("Currencies not found");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writer.println(new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                Collection<CurrencyView> currencyViews = currencies
                        .stream()
                                .map(currency -> {
                                    return new CurrencyView(
                                            currency.getId(),
                                            currency.getFullName(),
                                            currency.getCode(),
                                            currency.getSign()
                                    );
                                })
                                        .collect(Collectors.toList());
                writer.println(new GsonBuilder().create().toJson(currencyViews));
            } catch (Exception ex) {
                exceptionView.setMessage("Internal error");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.println(new GsonBuilder().create().toJson(exceptionView));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            try {
                Map<String, String> parametersMap = getPararametersAsMap(req);
                if (!isValidParameters(parametersMap)) {
                    exceptionView.setMessage("Fields: name, code, sign must be not empty and must be correct");
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writer.println(new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                String code = parametersMap.getOrDefault("code", null);
                String name = parametersMap.getOrDefault("name", null);
                String sign = parametersMap.getOrDefault("sign", null);
                if (currencyService.findByCode(code).isPresent()) {
                    exceptionView.setMessage("This currency already exist");
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    writer.println(new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                Optional<Currency> savedCurrency = currencyService.save(new Currency(code, name, sign));
                if (!savedCurrency.isPresent()) {
                    exceptionView.setMessage("Error save currency into database");
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    writer.println(new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                resp.setStatus(HttpServletResponse.SC_CREATED);
                writer.println(new GsonBuilder().create().toJson(savedCurrency.get()));
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
    public boolean isValidParameters(Map<String, String> parameters) {
        String code = parameters.getOrDefault("code", null);
        String name = parameters.getOrDefault("name", null);
        String sign = parameters.getOrDefault("sign", null);
        boolean isValid = true;
        if (code == null || code.isEmpty() ||
                name == null || name.isEmpty() ||
                sign == null || sign.isEmpty()) {
            isValid = false;
        }
        return isValid;
    }

    private Map<String, String> getPararametersAsMap(HttpServletRequest request) {
        Map<String, String> parameters = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            parameters.put(entry.getKey(), entry.getValue()[0]);
        }
        return parameters;
    }
}
