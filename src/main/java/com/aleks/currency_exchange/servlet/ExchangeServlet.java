package com.aleks.currency_exchange.servlet;

import com.aleks.currency_exchange.view.ExceptionView;
import com.aleks.currency_exchange.view.ExchangeView;
import com.aleks.currency_exchange.model.Currency;
import com.aleks.currency_exchange.model.ExchangeRate;
import com.aleks.currency_exchange.repository.CurrencyRepository;
import com.aleks.currency_exchange.repository.ExchangeRateRepository;
import com.aleks.currency_exchange.repository.SqliteCurrencyRepository;
import com.aleks.currency_exchange.repository.SqliteExchangeRateRepository;
import com.aleks.currency_exchange.service.CurrencyService;
import com.aleks.currency_exchange.service.ExchangeRateService;
import com.aleks.currency_exchange.validator.Validator;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet implements Validator {

    private ExchangeRateService exchangeRateService;
    private CurrencyService currencyService;
    private ExceptionView exceptionView;

    // todo review GET /exchange?from=BASE_CURRENCY_CODE&to=TARGET_CURRENCY_CODE&amount=$AMOUNT #
    // todo deploy
    // todo добавить проверку на отрицательные значения валюты
    // todo изменить на бигдецимал в модели валюты
    // todo удалить переменные репозиториев из сервлетов
    // todo  удалить templater

    @Override
    public void init(ServletConfig config) {
        exchangeRateService = new ExchangeRateService(new SqliteExchangeRateRepository());
        currencyService = new CurrencyService(new SqliteCurrencyRepository());
        exceptionView = new ExceptionView();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            try {
                Map<String, String> parameters = getParametersAsMap(req);
                if (!isValidParameters(parameters)) {
                    exceptionView.setMessage("Fields: from, to, amount must be not empty and must be correct");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            new GsonBuilder().create().toJson(exceptionView)
                    );
                    return;
                }
                String baseCodeCurr = parameters.getOrDefault("from", null);
                String targetCodeCurr = parameters.getOrDefault("to", null);
                double amount = Double.parseDouble(parameters.getOrDefault("amount", String.valueOf(0.0)));
                Optional<Currency> baseCurrency = currencyService.findByCode(baseCodeCurr.toUpperCase());
                Optional<Currency> targetCurrency = currencyService.findByCode(targetCodeCurr.toUpperCase());
                if (!baseCurrency.isPresent() || !targetCurrency.isPresent()) {
                    exceptionView.setMessage("Currencies with input codes not found");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, new GsonBuilder().create().toJson(exceptionView));
                    return;
                }
                Optional<ExchangeRate> dirExRate = exchangeRateService.findByCode(baseCurrency.get().getId(), targetCurrency.get().getId());
                if (dirExRate.isPresent()) {
                    ExchangeView exchangeView = new ExchangeView(
                            baseCurrency.get(),
                            targetCurrency.get(),
                            dirExRate.get().getRate(),
                            amount,
                            BigDecimal.valueOf(dirExRate.get().getRate() * amount)
                    );
                    writer.println(new GsonBuilder().create().toJson(exchangeView));
                    return;
                }
                Optional<ExchangeRate> backExRate = exchangeRateService.findByCode(targetCurrency.get().getId(), baseCurrency.get().getId());
                if (backExRate.isPresent()) {
                    ExchangeView exchangeView = new ExchangeView(
                            targetCurrency.get(),
                            baseCurrency.get(),
                            backExRate.get().getRate(),
                            amount,
                            BigDecimal.valueOf(dirExRate.get().getRate() * amount)
                    );
                    writer.println(new GsonBuilder().create().toJson(exchangeView));
                    return;
                }
                ExchangeRate[] exchangeRates = getTargetsCurrenciesId(baseCurrency.get().getId(), targetCurrency.get().getId());
                double rate = (exchangeRates[1].getRate() / exchangeRates[0].getRate());
                ExchangeView exchangeView = new ExchangeView(
                        baseCurrency.get(),
                        targetCurrency.get(),
                        rate,
                        amount,
                        BigDecimal.valueOf(dirExRate.get().getRate() * amount)
                );
                writer.println(new GsonBuilder().create().toJson(exchangeView));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private ExchangeRate[] getTargetsCurrenciesId(int baseCurrId, int targetCurrId) {
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

    @Override
    public boolean isValidParameters(Map<String, String> parameters) {
        boolean isValid = true;
        String baseCodeCurr = parameters.getOrDefault("from", null);
        String targetCodeCurr = parameters.getOrDefault("to", null);
        String amount = parameters.getOrDefault("amount", null);
        if (baseCodeCurr == null || baseCodeCurr.isEmpty() ||
                targetCodeCurr == null || targetCodeCurr.isEmpty() ||
                amount == null || amount.isEmpty()) {
            isValid = false;
        }
        return isValid;
    }

    private Map<String, String> getParametersAsMap(HttpServletRequest request) {
        String baseCurrCode = request.getParameter("from");
        String targetCurrCode = request.getParameter("to");
        String amount = request.getParameter("amount");
        Map<String, String> parameters = Map.of(
                "from", baseCurrCode,
                "to", targetCurrCode,
                "amount", amount
        );
        return parameters;
    }
}

