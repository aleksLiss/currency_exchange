package com.aleks.currency_exchange.validator;

import java.util.Map;

public interface Validator {

    boolean isValidParameters(Map<String, String> parameters);
}
