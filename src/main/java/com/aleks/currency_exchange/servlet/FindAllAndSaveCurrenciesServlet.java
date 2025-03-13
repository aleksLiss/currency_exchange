package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.repository.CurrencyRepository;
import com.aleks.currency_exchange.repository.SqliteCurrencyRepository;
import com.aleks.currency_exchange.service.CurrencyService;
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

// http://localhost:8080/currency_exchange/currencies

@WebServlet("/currencies")
public class FindAllAndSaveCurrenciesServlet extends HttpServlet implements Templater, Validator {

    private CurrencyService currencyService;
    private CurrencyRepository currencyRepository;

    @Override
    public void init(ServletConfig config) {
        currencyRepository = new SqliteCurrencyRepository();
        currencyService = new CurrencyService(currencyRepository);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            try {
                resp.setContentType("text/html;encoding=utf-8");
                Collection<Currency> currencies = currencyService.findAll();
                if (currencies.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Currencies not found");
                    return;
                }
                writer.println(getTemplate(new GsonBuilder().create().toJson(currencies)));
            } catch (Exception ex) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
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
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Fields: 'name', 'code', 'sign' must be not empty and must be correct");
                    return;
                }
                String code = parametersMap.getOrDefault("code", null);
                String name = parametersMap.getOrDefault("name", null);
                String sign = parametersMap.getOrDefault("sign", null);
                if (currencyService.findByCode(code).isPresent()) {
                    resp.sendError(HttpServletResponse.SC_CONFLICT, "This currency already exist");
                    return;
                }
                Optional<Currency> savedCurrency = currencyService.save(new Currency(code, name, sign));
                if (!savedCurrency.isPresent()) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error save currency into database");
                    return;
                }
                writer.println(getTemplate(new GsonBuilder().create().toJson(savedCurrency.get())));
            } catch (Exception ex) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getTemplate(String json) {
        return "<html><body>" +
                json +
                "</body></html>";
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
