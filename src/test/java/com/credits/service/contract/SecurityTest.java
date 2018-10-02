package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.thrift.generated.Variant;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Arrays;
import java.util.Collection;

import static com.credits.thrift.generated.Variant._Fields.V_I32;
import static com.credits.thrift.generated.Variant._Fields.V_STRING;
import static java.io.File.separator;
import static org.junit.Assert.fail;
import static org.junit.runners.Parameterized.Parameter;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SecurityTest extends ServiceTest {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    private final static String prjDir = "\"" + System.getProperty("user.dir") + separator + "credits" + separator + "file.test" + "\"";

    @Parameter
    public String methodName;
    @Parameter(1)
    public Variant arg;
    @Parameter(2)
    public boolean errorExpected;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"openSocket",  new Variant(V_I32, 5555), true},
            {"setTotal", new Variant(V_I32, 1000), false},
            {"getTotal", null, false},
            {"createFile", null, true},
            {"createFileInProjectDir", new Variant(V_STRING, prjDir), true},
            {"killProcess", null, true},
            {"newThread", null, false},
        });
    }

    byte[] bytecode;
    byte[] contractState;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        bytecode = compileSourceCode("/securityTest/Contract.java");
        contractState = ceService.execute(address, bytecode, null, null, null).getContractState();
    }

    @Test
    public void test() throws Exception {
        try {
            ceService.execute(address, bytecode, contractState, methodName, arg != null ? new Variant[] {arg} : new Variant[]{});
        } catch (ContractExecutorException e) {
            System.out.println(e.getMessage());
            if (!errorExpected || !e.getMessage().contains("AccessControlException")) {
                throw new Exception(e.getMessage());
            }
            return;
        }
        if (errorExpected) {
            fail("error expected");
        }
    }
}
