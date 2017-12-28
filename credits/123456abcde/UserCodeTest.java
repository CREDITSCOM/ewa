public class UserCodeTest {
    public static void main(String[] args) {
        System.out.println("Method main(String[] args) has been invoked");
        
        for (String arg : args) {
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

    public void foo(String str, Integer i, Float f) {
        System.out.println("Method foo(String str, Integer i, Float f) has been invoked");

        System.out.println("String: " + str);
        System.out.println("Integer: " + i);
        System.out.println("Float: " + f);
        
        System.out.println("Done");
    }

    public void foo(String str, Byte b, Float f) {
        System.out.println("Method foo(String str, Byte b, Float f) has been invoked");

        System.out.println("String: " + str);
        System.out.println("Byte: " + b);
        System.out.println("Float: " + f);

        System.out.println("Done");
    }

    public void foo(String str, Short s, Float f) {
        System.out.println("Method foo(String str, Short s, Float f) has been invoked");

        System.out.println("String: " + str);
        System.out.println("Short: " + s);
        System.out.println("Float: " + f);

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

}
