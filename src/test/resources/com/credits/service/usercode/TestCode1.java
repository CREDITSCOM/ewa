import com.credits.service.db.leveldb.LevelDbInteractionService;

public class TestCode1 {

    private static LevelDbInteractionService service;

    public TestCode1() throws Exception {
        System.out.println("The constructor has been invoked");

        Double balance = service.getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance);
    }
}