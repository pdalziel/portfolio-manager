CREATE TABLE equity(
    epic VARCHAR(24) PRIMARY KEY,
    company_name VARCHAR(512) NOT NULL,
    asset_type VARCHAR(256),
    sector VARCHAR(256),
    currency VARCHAR(4)
);

CREATE TABLE price(
    epic VARCHAR(24),
    year VARCHAR(24),
    price VARCHAR(24) NOT NULL,
    currency VARCHAR(3),
    quarter VARCHAR(24),
    PRIMARY KEY(epic, quarter)
);
