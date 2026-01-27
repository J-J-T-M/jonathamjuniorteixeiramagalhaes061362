CREATE TABLE regionals
(
    id         BIGINT PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    active     BOOLEAN   DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);