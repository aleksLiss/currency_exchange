package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.converter.Converter;
import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.repository.CurrencyRepository;
import com.aleks.currency_exchange.repository.SqliteCurrencyRepository;
import com.aleks.currency_exchange.service.CurrencyService;
import com.aleks.currency_exchange.templater.Templater;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.util.Optional;

// http://localhost:8080/currency_exchange/currencies

@WebServlet("/currencies")
public class FindAllOrSaveCurrenciesServlet extends HttpServlet implements Converter, Templater {

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
                writer.println(getTemplate(toJson()));
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
                resp.setContentType("text/html;charset=utf-8");
                writer.println("<html><body>");
                String code = req.getParameter("code");
                String name = req.getParameter("name");
                String sign = req.getParameter("sign");
                if (code == null || code.isEmpty() ||
                        name == null || name.isEmpty() ||
                        sign == null || sign.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Fields: 'name', 'code' and 'sign' must be not empty");
                    return;
                }
                if (currencyService.findByCode(code).isPresent()) {
                    resp.sendError(HttpServletResponse.SC_CONFLICT, "This currency already exist");
                    return;
                }
                final Currency gotCurrency = new Currency(code, name, sign);
                Optional<Currency> savedCurrency = currencyService.save(gotCurrency);
                if (savedCurrency.isPresent()) {
                    writer.println(new GsonBuilder().create().toJson(savedCurrency.get()));
                } else {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error get saved currency from database");
                }
            } catch (Exception ex) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Data base not accessed");
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    @Override
    public String getTemplate(Optional<?> content) {
        return content.map(s -> "<html><body>" + s + "</body></html>")
                .orElse("<html><body>" + "Database has not currencies" + "</body></html>");
    }

    @Override
    public Optional<String> toJson(String... code) {
        String toJson = null;
        try {
            Optional<Currency> foundCurrency = currencyService.findByCode(code[0]);
            final Gson gson = new GsonBuilder().create();
            toJson = gson.toJson(foundCurrency);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.ofNullable(toJson);
    }
}
