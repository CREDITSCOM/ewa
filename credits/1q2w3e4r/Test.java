public class Test {
    public static void main(String[] args) {
        System.out.println("Method main(String[] args) has been invoked");
        if (args == null || args.length == 0) {
            System.out.println("Args are empty");
        } else {
            for (String arg : args) {
                System.out.println(arg);
            }	
	}
        
        System.out.println("Done");
    }

//    public void foo() {
//        System.out.println("Method foo() has been invoked");
//        System.out.println("This method has no parameters");
//        System.out.println("Done");
//    }

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
}
