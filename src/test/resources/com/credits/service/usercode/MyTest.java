import com.credits.service.db.leveldb.LevelDbInteractionService;

import javax.inject.Inject;

public class MyTest {

    @Inject
    private LevelDbInteractionService service;

    public void foo() throws Exception {
        System.out.println("getBalance()");
        Double balance = service.getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance);
    }
}