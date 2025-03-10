package com.aleks.currency_exchange.templater;

import java.util.Optional;

public interface Templater {

    String getTemplate(Optional<String> content);
}
