import com.credits.service.db.leveldb.LevelDbInteractionService;

import javax.inject.Inject;

public class MyTest {

    private int i = 1;
    private static Integer is = 2;

    @Inject
    private LevelDbInteractionService service;

    public void foo() throws Exception {
        System.out.println("getBalance()");
        Double balance = service.getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance);

        System.out.println("i = " + i++);
        System.out.println("is = " + is++);
    }
}