create unique index if not exists base_target_currencies
on exchange_rates(base_currency_id, target_currency_id);
