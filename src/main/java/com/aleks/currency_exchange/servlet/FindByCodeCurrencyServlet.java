package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.repository.CurrencyRepository;
import com.aleks.currency_exchange.repository.SqliteCurrencyRepository;
import com.aleks.currency_exchange.service.CurrencyService;
import com.aleks.currency_exchange.view.ExceptionView;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.util.Optional;

// http://localhost:8080/currency_exchange/currency/USD

@WebServlet("/currency/*")
public class FindByCodeCurrencyServlet extends HttpServlet {

    private CurrencyRepository currencyRepository;
    private CurrencyService currencyService;
    private ExceptionView exceptionView;
    @Override
    public void init(ServletConfig config) {
        currencyRepository = new SqliteCurrencyRepository();
        currencyService = new CurrencyService(currencyRepository);
        exceptionView = new ExceptionView();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            try {
                String pathInfo = req.getPathInfo();
                if (pathInfo.split("/").length < 1) {
                    exceptionView.setMessage("Currency field must contains codeCurrency");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                String codeCurrency = req.getPathInfo().split("/")[1].toUpperCase();
                Optional<Currency> currency = currencyService.findByCode(codeCurrency);
                if (!currency.isPresent()) {
                    exceptionView.setMessage("Currency not found");
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                writer.println(new GsonBuilder().create().toJson(currency.get()));
            } catch (Exception ex) {
                exceptionView.setMessage("Database not accessed");
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new GsonBuilder().create().toJson(exceptionView));
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
