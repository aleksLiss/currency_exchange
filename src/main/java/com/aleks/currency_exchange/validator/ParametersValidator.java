package com.aleks.currency_exchange.validator;

import java.util.Map;

public interface ParametersValidator {

    boolean isValidParameters(Map<String, String> parameters);

}
