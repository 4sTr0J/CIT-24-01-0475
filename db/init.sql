CREATE DATABASE IF NOT EXISTS bank;

USE bank;

-- Drop tables if they exist to allow clean restarts
DROP TABLE IF EXISTS transaction_log;
DROP TABLE IF EXISTS account;

CREATE TABLE account (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    balance DOUBLE NOT NULL DEFAULT 0.0
);

CREATE TABLE transaction_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DOUBLE NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(255),
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE
);

-- Seed initial accounts
INSERT INTO account (name, account_number, balance) VALUES ('Alice Smith', 'ACC-1001', 1500.00);
INSERT INTO account (name, account_number, balance) VALUES ('Bob Jones', 'ACC-1002', 2500.00);
INSERT INTO account (name, account_number, balance) VALUES ('Charlie Brown', 'ACC-1003', 120.50);