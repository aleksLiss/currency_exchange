package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.view.ExchangeRateView;
import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.model.ExchangeRate;
import com.aleks.currency_exchange.repository.CurrencyRepository;
import com.aleks.currency_exchange.repository.ExchangeRateRepository;
import com.aleks.currency_exchange.repository.SqliteCurrencyRepository;
import com.aleks.currency_exchange.repository.SqliteExchangeRateRepository;
import com.aleks.currency_exchange.service.CurrencyService;
import com.aleks.currency_exchange.service.ExchangeRateService;
import com.aleks.currency_exchange.templater.Templater;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.util.Optional;

//      http://localhost:8080/currency_exchange/exchangeRate/usdeur
//      http://localhost:8080/currency_exchange/exchangeRate/usereur?rate=1000.00

@WebServlet("/exchangeRate/*")
public class FindByCodeAndUpdateExchangeRateServlet extends HttpServlet implements Templater {

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            try {
                resp.setContentType("text/html;encoding=utf-8");
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
                }
                ;
                ExchangeRateView exchangeRateView = new ExchangeRateView(
                        exchangeRate.get().getId(),
                        baseCurrency.get(),
                        targetCurrency.get(),
                        exchangeRate.get().getRate()
                );
                writer.println(getTemplate(new GsonBuilder().create().toJson(exchangeRateView)));
            } catch (Exception exception) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Exchange rate field must contains two code of currencies");
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
                String path = req.getPathInfo();
                String[] arrOfPath = path.split("/");
                String rate = req.getParameter("rate");
                if (arrOfPath.length == 0) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty codes of currencies");
                    return;
                }
                if (rate == null || rate.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty field 'rate'");
                    return;
                }
                String codesOfCurrencies = arrOfPath[1];
                String codeOfBaseCurrency = codesOfCurrencies.substring(0, 3).toUpperCase();
                String codeOfTargetCurrency = codesOfCurrencies.substring(3).toUpperCase();
                Optional<Currency> baseCurrency = currencyService.findByCode(codeOfBaseCurrency);   // empty
                Optional<Currency> targetCurrency = currencyService.findByCode(codeOfTargetCurrency);
                if (!baseCurrency.isPresent() || !targetCurrency.isPresent()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Base or target currency not found");
                    return;
                }
                Optional<ExchangeRate> foundExchangeRate = exchangeRateService.findByCode(baseCurrency.get().getId(), targetCurrency.get().getId()); // empty
                if (!foundExchangeRate.isPresent()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Exchange rate not found");
                    return;
                }
                boolean isUpdated = exchangeRateService.update(
                        new ExchangeRate(
                                foundExchangeRate.get().getId(),
                                baseCurrency.get().getId(),
                                targetCurrency.get().getId(),
                                Double.parseDouble(req.getParameter("rate"))
                        )
                );
                if (!isUpdated) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error update exchange rate");
                    return;
                }
                Optional<ExchangeRate> updatedExchangeRate = exchangeRateService.findByCode(baseCurrency.get().getId(), targetCurrency.get().getId());
                ExchangeRateView exchangeRateView = new ExchangeRateView(
                        updatedExchangeRate.get().getId(),
                        baseCurrency.get(),
                        targetCurrency.get(),
                        updatedExchangeRate.get().getRate()
                );
                writer.println(getTemplate(new GsonBuilder().create().toJson(exchangeRateView)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTemplate(String content) {
        return "<html><body>" +
                content +
                "</body></html>";
    }
}
