import com.credits.exception.ContractExecutorException;
import com.credits.service.db.leveldb.LevelDbInteractionService;
import com.credits.vo.usercode.Transaction;

import javax.inject.Inject;

public class MyTest {

    private static final String ADDRESS1 = "1a2b3c";
    private static final String ADDRESS2 = "123";
    private static final String ADDRESS3 = "1a2b";

    @Inject
    private LevelDbInteractionService service;

    public void addTransactions(int amount, int rounds) throws ContractExecutorException {
        System.out.println("enter method addTransactions");
        for (int i = 0; i < rounds; i++) {
            if (i % 2 == 0) {
                service.put(new Transaction(ADDRESS1, amount, '+'));
            } else {
                service.put(new Transaction(ADDRESS2, amount, '+'));
            }
        }
    }

    public void getBalance() {
        Transaction[] address1Transactions = service.get(ADDRESS1, 0);
        long address1balance = 0;
        for (Transaction tr : address1Transactions) {
            address1balance += tr.getValue();
        }
        System.out.println(ADDRESS1 + " balance: " + address1balance);

        Transaction[] address2Transactions = service.get(ADDRESS2, 0);
        long address2balance = 0;
        for (Transaction tr : address2Transactions) {
            address1balance += tr.getValue();
        }
        System.out.println(ADDRESS2 + " balance: " + address2balance);
    }

    public void work() {
        System.out.println("Method work is being involved...");
        if (service == null) {
            System.out.println("LevelDbInteractionService has not been initialized");
            return;
        }
        Transaction[] address3Transactions = service.get(ADDRESS3, 0);
        long address1balance = 0;
        for (Transaction tr : address3Transactions) {
            address1balance += tr.getValue();
        }
        System.out.println(ADDRESS3 + " balance: " + address1balance);
    }

}