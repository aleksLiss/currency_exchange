package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.repository.CurrencyRepository;
import com.aleks.currency_exchange.repository.SqliteCurrencyRepository;
import com.aleks.currency_exchange.service.CurrencyService;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Optional;

// http://localhost:8080/currency_exchange/currencies

@WebServlet("/currencies")
public class FindAllOrSaveCurrenciesServlet extends HttpServlet {

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
                writer.println("<html><body>");
                Collection<Currency> currencies = currencyService.findAll();
                if (currencies.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Currencies not found");
                    return;
                }
                writer.println(new GsonBuilder().create().toJson(currencies));
                writer.println("</body></html>");
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
                String code = req.getParameter("code");
                String name = req.getParameter("name");
                String sign = req.getParameter("sign");
                if (code == null || code.isEmpty() ||
                        name == null || name.isEmpty() ||
                        sign == null || sign.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Fields: 'name', 'code', 'sign' must be not empty");
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
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error save currency into database");
                }
            } catch (Exception ex) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Data base not accessed");
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
