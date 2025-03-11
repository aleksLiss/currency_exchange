package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.model.ExchangeRate;
import com.aleks.currency_exchange.repository.ExchangeRateRepository;
import com.aleks.currency_exchange.repository.SqliteExchangeRateRepository;
import com.aleks.currency_exchange.service.ExchangeRateService;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.util.Collection;

@WebServlet("/exchangeRates")
public class FindAllExchangeRatesServlet extends HttpServlet {

    private ExchangeRateService exchangeRateService;
    private ExchangeRateRepository exchangeRateRepository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        exchangeRateRepository = new SqliteExchangeRateRepository();
        exchangeRateService = new ExchangeRateService(exchangeRateRepository);
    }

    // http://localhost:8080/currency_exchange/exchangeRates

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            resp.setContentType("text/html;encoding=utf-8");
            writer.println("<html><body>");
            try {
                Collection<ExchangeRate> exchangeRates = exchangeRateService.findAll();
                if (exchangeRates.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Exchange rates not found");
                    return;
                }
                writer.println(new GsonBuilder().create().toJson(exchangeRates));
            } catch (Exception ex) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
            } finally {
                writer.println("</body></html>");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
