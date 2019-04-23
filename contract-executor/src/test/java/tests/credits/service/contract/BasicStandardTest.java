package tests.credits.service.contract;


import exception.ContractExecutorException;
import org.junit.Before;
import org.junit.Test;
import tests.credits.service.ServiceTest;

import static com.credits.utils.Constants.TOKEN_NAME_RESERVED_ERROR;

public class BasicStandardTest extends ServiceTest {

    public BasicStandardTest() {
        super("/serviceTest/MyBasicStandard.java");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testTokenName() throws Exception {
        try {
            deploySmartContract();
        } catch (ContractExecutorException e) {
            if (e.getMessage().equals(TOKEN_NAME_RESERVED_ERROR)) {
                assert true;
            } else {
                assert false;
            }

        }

    }

}

