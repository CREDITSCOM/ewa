public class Contract extends SmartContract {

    private static int statIntVar = 2;

    private int intVar = 1;

    public void initialize() {}

    public static void main(String[] args) {
        System.out.println("Method main(String[] args) has been invoked");
        
        for (String arg : args) {
            System.out.println(arg);
        }
        
        System.out.println("Done");
    }

    public static void main(Integer[] args) {
        System.out.println("Method main(Integer[] args) has been invoked");

        for (Integer arg : args) {
            System.out.println(arg);
        }

        System.out.println("Done");
    }

    public static void main(double[] args) {
        System.out.println("Method main(double[] args) has been invoked");

        for (double arg : args) {
            System.out.println(arg);
        }

        System.out.println("Done");
    }

    public void foo(String str, Double d, Integer i) {
        System.out.println("Method foo(String str, Double d, Integer i) has been invoked");

        System.out.println("String: " + str);
        System.out.println("Double: " + d);
        System.out.println("Integer: " + i);
        
        System.out.println("Done");
    }

    public void foo(String str, Long l, Float f) {
        System.out.println("Method foo(String str, Long l, Float f) has been invoked");

        System.out.println("String: " + str);
        System.out.println("Long: " + l);
        System.out.println("Float: " + f);

        System.out.println("Done");
    }

    public void foo(String str, int i, float f) {
        System.out.println("Method foo(String str, int i, float f) has been invoked");

        System.out.println("String: " + str);
        System.out.println("int: " + i);
        System.out.println("float: " + f);
        
        System.out.println("Done");
    }

    public void foo(String str, Byte b, Float f) {
        System.out.println("Method foo(String str, Byte b, Float f) has been invoked");

        System.out.println("String: " + str);
        System.out.println("Byte: " + b);
        System.out.println("Float: " + f);

        System.out.println("Done");
    }

    public void foo(String str, short s, float f) {
        System.out.println("Method foo(String str, short s, float f) has been invoked");

        System.out.println("String: " + str);
        System.out.println("short: " + s);
        System.out.println("float: " + f);

        System.out.println("Done");
    }

    static public void foo() {
        System.out.println("Static method without params has been invoked");
    }

    static public void foo(String str, Short s, Integer i) {
        System.out.println("Static method foo(String str, Short s, Integer i) has been invoked");
        System.out.println("String: " + str);
        System.out.println("Short: " + s);
        System.out.println("Integer: " + i);
        System.out.println("Done");
    }

    public void foo(boolean[] args) {
        System.out.println("Method foo(boolean[] args) has been invoked");
        for (boolean arg : args) {
            System.out.println(arg);
        }
        System.out.println("Done");
    }

    public void foo(int[] args) {
        System.out.println("Method foo(int[] args) has been invoked");
        for (int arg : args) {
            System.out.println(arg);
        }
        System.out.println("Done");
    }

    public void foo(short[] args) {
        System.out.println("Method foo(short[] args) has been invoked");
        for (short arg : args) {
            System.out.println(arg);
        }
        System.out.println("Done");
    }

    public void foo(Long[] args) {
        System.out.println("Method foo(Long[] args) has been invoked");
        for (Long arg : args) {
            System.out.println(arg);
        }
        System.out.println("Done");
    }

    public void foo(Float[] args) {
        System.out.println("Method foo(Float[] args) has been invoked");
        for (Float arg : args) {
            System.out.println(arg);
        }
        System.out.println("Done");
    }

    public void foo(
            float param01,
            Float param02,
            int param03,
            Integer param04,
            double param05,
            Double param06,
            String[] param07,
            int[] param08,
            Integer[] param09,
            double[] param10,
            Double[] param11,
            boolean[] param12,
            Boolean[] param13,
            short[] param14,
            long[] param15,
            Long[] param16,
            float[] param17,
            Float[] param18
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
    }

    public void globalVarInstance() {
        System.out.println("intVar value = " + intVar);
        intVar++;
    }

    public static void globalVarStatic() {
        System.out.println("statIntVar value = " + statIntVar);
        statIntVar++;
    }

}
