package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.mapper.ExchangeRateMapper;
import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.model.ExchangeRate;
import com.aleks.currency_exchange.repository.CurrencyRepository;
import com.aleks.currency_exchange.repository.ExchangeRateRepository;
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
import java.util.Collection;
import java.util.Optional;

@WebServlet("/exchangeRates")
public class FindAllExchangeRatesServlet extends HttpServlet {

    private ExchangeRateService exchangeRateService;
    private ExchangeRateRepository exchangeRateRepository;
    private CurrencyService currencyService;
    private CurrencyRepository currencyRepository;

    @Override
    public void init(ServletConfig config) {
        currencyRepository = new SqliteCurrencyRepository();
        currencyService = new CurrencyService(currencyRepository);
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            resp.setContentType("text/html;encoding=utf-8");
            writer.println("<html><body>");
            try {
                String baseCurrencyCode = req.getParameter("baseCurrencyCode");
                String targetCurrencyCode = req.getParameter("targetCurrencyCode");
                String rate = req.getParameter("rate");
                if ((baseCurrencyCode == null || baseCurrencyCode.isEmpty()) ||
                        (targetCurrencyCode == null || targetCurrencyCode.isEmpty()) ||
                        (rate == null || rate.isEmpty())) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Fields: 'baseCurrencyCode', 'targetCurrencyCode', 'rate' must be not empty");
                    return;
                }
                Optional<Currency> foundBaseCurrency = currencyService.findByCode(baseCurrencyCode.toUpperCase());
                Optional<Currency> foundTargetCurrency = currencyService.findByCode(targetCurrencyCode.toUpperCase());
                if (!foundTargetCurrency.isPresent() || !foundBaseCurrency.isPresent()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Base currency or target currency not found");
                    return;
                }
                Optional<ExchangeRate> foundExchangeRate = exchangeRateService.findByCode(
                        foundBaseCurrency.get().getId(),
                        foundTargetCurrency.get().getId());
                if (foundExchangeRate.isPresent()) {
                    resp.sendError(HttpServletResponse.SC_CONFLICT, "Input exchange rate already exist");
                    return;
                }
                Optional<ExchangeRate> savedExchangeRate = exchangeRateService.save(
                        new ExchangeRate(
                                foundBaseCurrency.get().getId(),
                                foundTargetCurrency.get().getId(),
                                Double.parseDouble(rate))
                );
                if (savedExchangeRate.isPresent()) {
                    ExchangeRateMapper mapper = new ExchangeRateMapper(
                            savedExchangeRate.get().getId(),
                            foundBaseCurrency.get(),
                            foundTargetCurrency.get(),
                            savedExchangeRate.get().getRate()
                            );
                    writer.println(new GsonBuilder().create().toJson(mapper));
                } else {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error save new exchange rate into database");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                writer.println("</body></html>");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
