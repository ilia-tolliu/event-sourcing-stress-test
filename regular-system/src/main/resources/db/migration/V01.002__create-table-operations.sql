CREATE TABLE operations
(
    operations_tx_id      UUID           NOT NULL,
    accounts_id           UUID           NOT NULL REFERENCES accounts,
    operations_name       text           NOT NULL,
    operations_debit      NUMERIC(10, 2) NOT NULL,
    operations_credit     NUMERIC(10, 2) NOT NULL,
    operations_created_at TIMESTAMPTZ,
    PRIMARY KEY (operations_tx_id, accounts_id, operations_name)
);
