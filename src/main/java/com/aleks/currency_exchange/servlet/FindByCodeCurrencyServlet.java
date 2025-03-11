package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.repository.CurrencyRepository;
import com.aleks.currency_exchange.repository.SqliteCurrencyRepository;
import com.aleks.currency_exchange.service.CurrencyService;
import com.aleks.currency_exchange.templater.Templater;
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
public class FindByCodeCurrencyServlet extends HttpServlet implements Templater {

    private CurrencyRepository currencyRepository;
    private CurrencyService currencyService;

    @Override
    public void init(ServletConfig config) {
        currencyRepository = new SqliteCurrencyRepository();
        currencyService = new CurrencyService(currencyRepository);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            resp.setContentType("text/html;encoding=utf-8");
            String pathInfo = req.getPathInfo();
            if (pathInfo.split("/").length < 1) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency field must contains codeCurrency");
            } else {
                String codeCurrency = req.getPathInfo().split("/")[1].toUpperCase();
                Optional<Currency> currency = currencyService.findByCode(codeCurrency);
                if (currency.isPresent()) {
                    writer.println(getTemplate(currency));
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Currency not found");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getTemplate(Optional<?> content) {
        Optional<Currency> optionalCurrency = (Optional<Currency>) content;
        StringBuilder builder = new StringBuilder();
        return optionalCurrency.map(currency -> builder.append("<html><body>")
                .append(new GsonBuilder().create().toJson(currency))
                .append("</body></html>")
                .toString()).orElseGet(() -> builder.append("<html><body>")
                .append("[]")
                .append("</body></html>").toString());
    }
}
