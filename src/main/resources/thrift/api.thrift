
namespace java com.credits.thrift.gen.api

typedef string Currency;
typedef string Address;
typedef i64 Time;

struct Amount 
{
  1: required i32 integral = 0;
  2: required i64 fraction = 0;
}

typedef map<Currency, Amount> Balance;

//
// Transactions
//

typedef string TransactionHash
typedef string TransactionId
typedef string TransactionInnerId

struct Transaction
{
    1: TransactionHash hash
    2: TransactionInnerId innerId
    3: Address source
    4: Address target
    5: Amount amount
    6: Currency currency
}

typedef list<Transaction> Transactions
typedef list<TransactionId> TransactionIds

//
//  Pools
//

typedef string PoolHash
typedef i64 PoolNumber

struct Pool
{
    1: PoolHash poolHash
    2: PoolHash prevPoolHash
    3: Time creationTime
    4: PoolNumber poolNumber
    5: i32 transactionsCount
}

typedef list<Pool> Pools

//
// API responses
//

struct APIResponse
{
    1: i8 code
    2: string message
}

// PoolListGet

struct PoolListGetResult
{
    1: APIResponse status
    2: bool result
    3: Pools pools
}

// PoolGet

struct PoolGetResult
{
    1: APIResponse status
    2: Pool pool
    3: Transactions transactions
}

// BalanceGet

struct BalanceGetResult
{
    1: APIResponse status
    2: Amount amount
}

// TransactionGet

struct TransactionGetResult
{
    1: APIResponse status
    2: bool found
    3: Transaction transaction
}

// TransactionsGet

struct TransactionsGetResult
{
    1: APIResponse status
    2: bool result
    3: Transactions transactions
}

service API 
{
    BalanceGetResult BalanceGet(1:Address address, 2:Currency currency = 'CS')
   
    TransactionGetResult TransactionGet(1:TransactionId transactionId)
    TransactionsGetResult TransactionsGet(1:Address address, 2:i64 offset, 3:i64 limit)
    
    PoolListGetResult PoolListGet(1:i64 offset, 2:i64 limit)
    PoolGetResult PoolGet(1:PoolNumber poolNumber)
}
