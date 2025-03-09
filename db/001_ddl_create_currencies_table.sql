create table currencies(
    id integer primary key,
    code varchar unique not null,
    full_name varchar not null,
    sign varchar not null
);