create table exchange_rates(
    id integer primary key,
    base_currency_id integer references currencies(id),
    target_currency_id integer references currencies(id),
    rate decimal(6)
);

