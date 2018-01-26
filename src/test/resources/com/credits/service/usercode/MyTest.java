import com.credits.exception.ContractExecutorException;
import com.credits.service.db.leveldb.LevelDbInteractionService;
import com.credits.thrift.gen.api.Amount;
import com.credits.thrift.gen.api.TransactionInfo;
import com.credits.thrift.gen.api.Transaction;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class MyTest {

    private static final String ADDRESS1 = "1a2b3c";
    private static final String ADDRESS2 = "123";
    private static final String ADDRESS3 = "1a2b";

    @Inject
    private LevelDbInteractionService service;

//    public void work() {
//        System.out.println("Method work is being involved...");
//        if (service == null) {
//            System.out.println("LevelDbInteractionService has not been initialized");
//            return;
//        }
//        Transaction[] address3Transactions = service.get(ADDRESS3, 0);
//        long address1balance = 0;
//        for (Transaction tr : address3Transactions) {
//            address1balance += tr.getValue();
//        }
//        System.out.println(ADDRESS3 + " balance: " + address1balance);
//    }

    public void foo() throws ContractExecutorException {
        System.out.println("get_balance()");
        Map<String,Amount> balance = service.getBalance("3QJmV3qfvL9SuYo34YihAf3sRCW3qSinyC");
        System.out.println("get_balance=" + balance);

        System.out.println("get_transactions()");
        List<Transaction> transactions = service.getTransactions("3QJmV3qfvL9SuYo34YihAf3sRCW3qSinyC", "BTC");
        System.out.println("get_transactions=" + transactions);

        System.out.println("get_transaction_info()");
        TransactionInfo info = service.getTransactionInfo("3QJmV3qfvL9SuYo34YihAf3sRCW3qSinyC", "3QvxvxuotS5PuTjmVUpWN6sVkfzUfX3RFV", new Amount(13, 37), 0, "DASH");
        System.out.println("get_transaction_info=" + info);
    }

    public void foo1() throws ContractExecutorException {
        System.out.println("get_balance()");
        Map<String,Amount> balance = service.getBalance("123");
        System.out.println("get_balance=" + balance);
    }

}