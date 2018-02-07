import com.credits.service.db.leveldb.LevelDbInteractionService;

import javax.inject.Inject;
import java.util.Arrays;

public class MyTest {

    private int i = 1;
    private static Integer is = 2;

    private int[] iArray = {1, 2, 3};
    private Long[] lArray = {1l, 2l, 3l};
    private String[] sArray = {"dmitrii", "alexandr"};

    @Inject
    private LevelDbInteractionService service;

    public void foo() throws Exception {
        System.out.println("getBalance()");
        Double balance = service.getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance);

        System.out.println("i = " + i++);
        System.out.println("is = " + is++);

        System.out.println("int[] iArray = " + Arrays.toString(iArray));
        for (int i = 0; i < iArray.length; i++) {
            iArray[i]++;
        }

        System.out.println("Long[] lArray = " + Arrays.toString(lArray));
        for (int i = 0; i < iArray.length; i++) {
            lArray[i]++;
        }

        System.out.println("String[] sArray = " + Arrays.toString(sArray));
        for (int i = 0; i < sArray.length; i++) {
            sArray[i] = sArray[i] + 1;
        }
    }
}