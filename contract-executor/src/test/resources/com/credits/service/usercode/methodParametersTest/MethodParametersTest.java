import com.credits.scapi.v0.SmartContract;

import java.util.List;
import java.util.ArrayList;

public class MethodParametersTest extends SmartContract {

    private static int statIntVar = 2;

    private int intVar = 1;

    public void initialize() {}

    public MethodParametersTest(){
        super();
    }

    public static Integer mainString(List<String> args) {
        System.out.println("Method main(List<String> args) has been invoked");
        
        for (String arg : args) {
            System.out.println(arg);
        }
        
        System.out.println("Done");
        return 1;
    }

    public static Integer mainInteger(List<Integer> args) {
        System.out.println("Method main(List<Integer> args) has been invoked");

        for (Integer arg : args) {
            System.out.println(arg);
        }

        System.out.println("Done");
        return 1;
    }

    public static Integer main(List<Double> args) {
        System.out.println("Method main(List<Double> args) has been invoked");

        for (double arg : args) {
            System.out.println(arg);
        }

        System.out.println("Done");
        return 1;
    }

    public Integer foo(String str, Double d, Integer i) {
        System.out.println("Method foo(String str, Double d, Integer i) has been invoked");

        System.out.println("String: " + str);
        System.out.println("Double: " + d);
        System.out.println("Integer: " + i);
        
        System.out.println("Done");
        return 1;
    }

    public Integer foo(String str, Long l, Float f) {
        System.out.println("Method foo(String str, Long l, Float f) has been invoked");

        System.out.println("String: " + str);
        System.out.println("Long: " + l);
        System.out.println("Float: " + f);

        System.out.println("Done");
        return 1;
    }

    public Integer foo(String str, int i, float f) {
        System.out.println("Method foo(String str, int i, float f) has been invoked");

        System.out.println("String: " + str);
        System.out.println("int: " + i);
        System.out.println("float: " + f);
        
        System.out.println("Done");
        return 1;
    }

    public Integer foo(String str, Byte b, Float f) {
        System.out.println("Method foo(String str, Byte b, Float f) has been invoked");

        System.out.println("String: " + str);
        System.out.println("Byte: " + b);
        System.out.println("Float: " + f);

        System.out.println("Done");
        return 1;
    }

    public Integer foo(String str, short s, float f) {
        System.out.println("Method foo(String str, short s, float f) has been invoked");

        System.out.println("String: " + str);
        System.out.println("short: " + s);
        System.out.println("float: " + f);

        System.out.println("Done");
        return 1;
    }

    static public Integer foo() {
        System.out.println("Static method without params has been invoked");
        return 1;
    }

    static public Integer foo(String str, Short s, Integer i) {
        System.out.println("Static method foo(String str, Short s, Integer i) has been invoked");
        System.out.println("String: " + str);
        System.out.println("Short: " + s);
        System.out.println("Integer: " + i);
        System.out.println("Done");
        return 1;
    }

    public Integer fooBoolean(List<Boolean> args) {
        System.out.println("Method foo(List<Boolean> args) has been invoked");
        for (boolean arg : args) {
            System.out.println(arg);
        }
        System.out.println("Done");
        return 1;
    }

    public Integer foo(List<Integer> args) {
        System.out.println("Method foo(List<Integer> args) has been invoked");
        for (int arg : args) {
            System.out.println(arg);
        }
        System.out.println("Done");
        return 1;
    }

    public Integer fooShort(List<Short> args) {
        System.out.println("Method foo(List<Short> args) has been invoked");
        for (short arg : args) {
            System.out.println(arg);
        }
        System.out.println("Done");
        return 1;
    }

    public Integer fooInteger(List<Integer> args) {
        System.out.println("Method foo(List<Long> args) has been invoked");
        for (Integer arg : args) {
            System.out.println(arg);
        }
        System.out.println("Done");
        return 1;
    }

    public Integer fooDouble(List<Double> args) {
        System.out.println("Method foo(List<Double> args) has been invoked");
        for (Double arg : args) {
            System.out.println(arg);
        }
        System.out.println("Done");
        return 1;
    }

    public Integer foo(
        double param01,
        Double param02,
        int param03,
        Integer param04,
        double param05,
        Double param06,
        List<String> param07,
        List<Integer> param08,
        List<Integer> param09,
        List<Double> param10,
        List<Double> param11,
        List<Boolean> param12,
        List<Boolean> param13,
        List<Short> param14,
        List<Long> param15,
        List<Long> param16,
        List<Double> param17,
        List<Double> param18
    ) {
        System.out.println("param01 = " + param01);
        System.out.println("param02 = " + param02);
        System.out.println("param03 = " + param03);
        System.out.println("param04 = " + param04);
        System.out.println("param05 = " + param05);
        System.out.println("param06 = " + param06);
        System.out.println("param07 = " + param07);
        System.out.println("param08 = " + param08);
        System.out.println("param09 = " + param09);
        System.out.println("param10 = " + param10);
        System.out.println("param11 = " + param11);
        System.out.println("param12 = " + param12);
        System.out.println("param13 = " + param13);
        System.out.println("param14 = " + param14);
        System.out.println("param15 = " + param15);
        System.out.println("param16 = " + param16);
        System.out.println("param17 = " + param17);
        System.out.println("param18 = " + param18);
        return 1;
    }

    public Integer food(
        double param01
    ) {
        System.out.println("param01 = " + param01);
        return 1;
    }

    public Integer food(
        Double param01
    ) {
        System.out.println("param01 = " + param01);
        return 1;
    }

    public Integer foo(
        double param01,
        Double param02,
        int param03,
        Integer param04,
        double param05,
        Double param06
    ) {
        System.out.println("param01 = " + param01);
        System.out.println("param02 = " + param02);
        System.out.println("param03 = " + param03);
        System.out.println("param04 = " + param04);
        System.out.println("param05 = " + param05);
        System.out.println("param06 = " + param06);
        return 1;
    }

    public Integer foo(
        double param01,
        Double param02,
        int param03,
        Integer param04,
        double param05,
        Double param06,
        ArrayList<String> param07
    ) {
        System.out.println("param01 = " + param01);
        System.out.println("param02 = " + param02);
        System.out.println("param03 = " + param03);
        System.out.println("param04 = " + param04);
        System.out.println("param05 = " + param05);
        System.out.println("param06 = " + param06);
        System.out.println("param07 = " + param07);
        return 1;
    }

    public Integer foo(
        double param01,
        Double param02,
        int param03,
        Integer param04,
        double param05,
        Double param06,
        List<Integer> param08
    ) {
        System.out.println("param01 = " + param01);
        System.out.println("param02 = " + param02);
        System.out.println("param03 = " + param03);
        System.out.println("param04 = " + param04);
        System.out.println("param05 = " + param05);
        System.out.println("param06 = " + param06);
        System.out.println("param08 = " + param08);
        return 1;
    }


    public Integer globalVarInstance() {
        System.out.println("intVar value = " + intVar);
        intVar++;
        return 1;
    }

    public static Integer globalVarStatic() {
        System.out.println("statIntVar value = " + statIntVar);
        statIntVar++;
        return 1;
    }

}
