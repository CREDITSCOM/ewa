
namespace java com.credits.thrift.gen.api

typedef binary Hash;

typedef string Currency;

typedef string Address;

struct Amount 
{
  1: required i32 integral = 0;
  2: required i64 fraction = 0;
}

typedef map<Currency, Amount> Balance;

struct Transaction 
{
  1: required Address source;

  2: required Address target;

  3: required Currency currency;

  4: required Amount amount;

  5: required Amount fee;

  6: required i64 time = 0;

  7: required i64 pool = 0;

  8: required i64 nonce = 0;
}

typedef list<Transaction> Transactions;

struct TransactionInfo 
{
  1: Hash hash;
  2: string status;
}

service API 
{
   Balance get_balance(1:Address address)

   Transactions get_transactions(1:Address address, 2:Currency currency = '')
   
   TransactionInfo get_transaction_info(1:Address source, 2:Address destination, 3:Amount amount, 4:i64 timestamp, 5:Currency currency = '')
}