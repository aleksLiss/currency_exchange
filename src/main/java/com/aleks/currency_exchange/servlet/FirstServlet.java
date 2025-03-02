package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.repository.CurrencyRepository;
import com.aleks.currency_exchange.repository.SqlLiteCurrencyRepository;
import com.aleks.currency_exchange.service.CurrencyService;
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
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;


@WebServlet("/currencies")
public class FirstServlet extends HttpServlet {

    private CurrencyService currencyService;
    private CurrencyRepository currencyRepository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        currencyRepository = new SqlLiteCurrencyRepository();
        currencyService = new CurrencyService(currencyRepository);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();

        final Gson gson = new GsonBuilder().create();
        try {
            writer.println("<html><body>");
            Collection<Currency> currencies = currencyService.findAll();
            writer.println(gson.toJson(currencies));
        } catch (Exception ex) {
            writer.println("500 Internal Server Error");
            ex.printStackTrace();
        } finally {
            writer.println("</body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        resp.setContentType("text/html;charset=utf-8");
        try {
            writer.println("<html><body>");
            String code = req.getParameter("code");
            String fullName = req.getParameter("fullName");
            String sign = req.getParameter("sign");
            if (code.isEmpty() || fullName.isEmpty() || sign.isEmpty()) {
                writer.println("400 Bad Request");
            }
            try {
                Optional<Currency> foundCurrency = currencyService.findByCode(code);
                if (foundCurrency.isPresent()) {
                    writer.println("409 Conflict");
                }
            } catch (ClassNotFoundException | SQLException ex) {
                writer.println("<h1>" + ex.getMessage() + "</h1>");
                ex.printStackTrace();
            }
            final Gson gson = new GsonBuilder().create();
            final Currency currency = new Currency(code, fullName, sign);
            Optional<Currency> savedCurrency = currencyService.save(currency);
            writer.println(gson.toJson(savedCurrency.get()));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            writer.println("</body></html>");
        }
    }
}
