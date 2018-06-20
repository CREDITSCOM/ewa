package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

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
    public String arg;
    @Parameter(2)
    public boolean errorExpected;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"openSocket", "5555", true},
            {"setTotal", "1000", false},
            {"getTotal", null, false},
            {"createFile", null, true},
            {"createFileInProjectDir", prjDir, true},
            {"killProcess", null, true},
            {"killThread", null, false},
            {"newThread", null, false},
        });
    }

    byte[] bytecode;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        bytecode = compileSourceCode("/securityTest/Contract.java");
    }

    @After
    public void tearDown() {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileSystemUtils.deleteRecursively(new File(dir));
    }
    @Test
    public void test() throws Exception {
        try {
            ceService.execute(address, bytecode, null, methodName, arg != null ? new String[] {arg} : null);
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
