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
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class FindByCodeAndUpdateExchangeRateServlet extends HttpServlet {

    private ExchangeRateRepository exchangeRateRepository;
    private ExchangeRateService exchangeRateService;
    private CurrencyRepository currencyRepository;
    private CurrencyService currencyService;

    @Override
    public void init(ServletConfig config) {
        exchangeRateRepository = new SqliteExchangeRateRepository();
        exchangeRateService = new ExchangeRateService(exchangeRateRepository);
        currencyRepository = new SqliteCurrencyRepository();
        currencyService = new CurrencyService(currencyRepository);
    }

    //      http://localhost:8080/currency_exchange/exchangeRate/usdeur
    //      http://localhost:8080/currency_exchange/exchangeRate/usereur?rate=1000.00

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            try {
                resp.setContentType("text/html;encoding=utf-8");
                writer.println("<html><body>");
                String path = req.getPathInfo();
                if (path.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Exchange rate field must contains two code of currencies");
                    return;
                }
                String[] codesBaseAndTargetCurrencies = getCodesCurrenciesFromPath(path);
                Optional<Currency> baseCurrency = currencyService.findByCode(codesBaseAndTargetCurrencies[0]);
                Optional<Currency> targetCurrency = currencyService.findByCode(codesBaseAndTargetCurrencies[1]);
                if (!baseCurrency.isPresent() || !targetCurrency.isPresent()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currencies with input codes not found");
                    return;
                }
                Optional<ExchangeRate> exchangeRate = exchangeRateService.findByCode(baseCurrency.get().getId(), targetCurrency.get().getId());
                if (!exchangeRate.isPresent()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Exchange rate not found");
                    return;
                };
                ExchangeRateMapper mapper = new ExchangeRateMapper(
                        exchangeRate.get().getId(),
                        baseCurrency.get(),
                        targetCurrency.get(),
                        exchangeRate.get().getRate()
                );
                writer.println(new GsonBuilder().create().toJson(mapper));
            } catch (Exception exception) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Exchange rate field must contains two code of currencies");
            } finally {
                writer.println("</body></html>");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String[] getCodesCurrenciesFromPath(String path) {
        String[] arrOfPath = path.split("/");
        String codesOfCurrencies = arrOfPath[1];
        String codeOfBaseCurrency = codesOfCurrencies.substring(0, 3).toUpperCase();
        String codeOfTargetCurrency = codesOfCurrencies.substring(3).toUpperCase();
        return new String[]{codeOfBaseCurrency, codeOfTargetCurrency};
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            resp.setContentType("text/html;encoding=utf-8");
            try {
                writer.println("<html><body>");
                String path = req.getPathInfo();
                String[] arrOfPath = path.split("/");
                String codesOfCurrencies = arrOfPath[1];
                String codeOfBaseCurrency = codesOfCurrencies.substring(0, 3).toUpperCase();
                String codeOfTargetCurrency = codesOfCurrencies.substring(3).toUpperCase();
                Optional<Currency> baseCurrency = currencyService.findByCode(codeOfBaseCurrency);
                Optional<Currency> targetCurrency = currencyService.findByCode(codeOfTargetCurrency);
                Optional<ExchangeRate> foundExchangeRate = exchangeRateService.findByCode(baseCurrency.get().getId(), targetCurrency.get().getId());
                writer.println(exchangeRateService.update(
                        new ExchangeRate(
                                foundExchangeRate.get().getId(),
                                baseCurrency.get().getId(),
                                targetCurrency.get().getId(),
                                Double.parseDouble(req.getParameter("rate"))
                        )
                ));
                writer.println(new GsonBuilder().create().toJson(exchangeRateService.findAll()));
                writer.println("</body></html>");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
