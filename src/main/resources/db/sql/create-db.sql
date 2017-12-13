CREATE TABLE contracts (
  id                 INTEGER AUTO_INCREMENT PRIMARY KEY,
  source_account     VARCHAR(80),
  sender             VARCHAR(50),
  transaction_amount INTEGER,
  dest_account       VARCHAR(80)
);