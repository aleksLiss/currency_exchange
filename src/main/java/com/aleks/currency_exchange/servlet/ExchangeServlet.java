package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.mapper.ExchangeMapper;
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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Optional;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private ExchangeRateRepository exchangeRateRepository;
    private static ExchangeRateService exchangeRateService;
    private static CurrencyRepository currencyRepository;
    private CurrencyService currencyService;


    @Override
    public void init(ServletConfig config) {
        exchangeRateRepository = new SqliteExchangeRateRepository();
        exchangeRateService = new ExchangeRateService(exchangeRateRepository);
        currencyRepository = new SqliteCurrencyRepository();
        currencyService = new CurrencyService(currencyRepository);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            resp.setContentType("text/html;encoding=utf-8");
            writer.println("<html><body>");
            try {
                String baseCodeCurr = req.getParameter("from");
                String targetCodeCurr = req.getParameter("to");
                String amountParameter = req.getParameter("amount");
                if (baseCodeCurr == null || baseCodeCurr.isEmpty() ||
                        targetCodeCurr == null || targetCodeCurr.isEmpty() ||
                        amountParameter == null || amountParameter.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Fields: 'from', 'to', 'amount' must be not empty");
                    return;
                }
                double amount = Double.parseDouble(req.getParameter("amount"));
                Optional<Currency> baseCurrency = currencyService.findByCode(baseCodeCurr.toUpperCase());
                Optional<Currency> targetCurrency = currencyService.findByCode(targetCodeCurr.toUpperCase());
                if (!baseCurrency.isPresent() || !targetCurrency.isPresent()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currencies with input codes not found");
                    return;
                }
                Optional<ExchangeRate> dirExRate = exchangeRateService.findByCode(baseCurrency.get().getId(), targetCurrency.get().getId());
                if (!dirExRate.isPresent()) {
                    try {
                        Optional<ExchangeRate> backExRate = exchangeRateService.findByCode(targetCurrency.get().getId(), baseCurrency.get().getId());
                        if (!backExRate.isPresent()) {
                            ExchangeRate[] exchangeRates = getTargetsCurrenciesId(baseCurrency.get().getId(), targetCurrency.get().getId());
                            double rate = (exchangeRates[1].getRate() / exchangeRates[0].getRate());
                            ExchangeMapper mapper = new ExchangeMapper(
                                    baseCurrency.get(),
                                    targetCurrency.get(),
                                    rate,
                                    amount,
                                    BigDecimal.valueOf(dirExRate.get().getRate() * amount)
                            );
                            writer.println(new GsonBuilder().create().toJson(mapper));
                        } else {
                            ExchangeMapper mapper = new ExchangeMapper(
                                    targetCurrency.get(),
                                    baseCurrency.get(),
                                    backExRate.get().getRate(),
                                    amount,
                                    BigDecimal.valueOf(dirExRate.get().getRate() * amount)
                            );
                            writer.println(new GsonBuilder().create().toJson(mapper));
                        }
                    } catch (Exception ex) {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Back Ex rate not found");
                    }
                } else {
                    ExchangeMapper mapper = new ExchangeMapper(
                            baseCurrency.get(),
                            targetCurrency.get(),
                            dirExRate.get().getRate(),
                            amount,
                            BigDecimal.valueOf(dirExRate.get().getRate() * amount)
                    );
                    writer.println(new GsonBuilder().create().toJson(mapper));
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

    private static ExchangeRate[] getTargetsCurrenciesId(int baseCurrId, int targetCurrId) {
        ExchangeRate[] result = new ExchangeRate[2];
        exchangeRateService.findAll()
                .forEach(exRate -> {
                    if (exRate.getBaseCurrencyId() == baseCurrId || exRate.getTargetCurrencyId() == baseCurrId) {
                        if (result[0] == null) {
                            result[0] = exRate;
                        }
                    }
                    ;
                    if (exRate.getTargetCurrencyId() == targetCurrId || exRate.getBaseCurrencyId() == targetCurrId) {
                        if (result[1] == null) {
                            result[1] = exRate;
                        }
                    }
                });
        return result;
    }

    ;
}

