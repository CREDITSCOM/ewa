package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.utils.EclipseJdt;
import org.eclipse.jdt.core.compiler.IProblem;
import org.junit.Test;

/**
 * Created by Rustem Saidaliyev on 27-Mar-18.
 */
public class EclipseJdtTest {

    @Test
    public void checkSyntaxTest() {
        String sourceCode =
                "public class Contract extends SmartContract {\n" +
                "\n" +
                "    private static int statIntVar = 2;\n" +
                "\n" +
                "    private int intVar = 1;\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Method main(String[] args) has been invoked\");\n" +
                "        \n" +
                "        for (String arg : args) {\n" +
                "            System.out.println(arg);\n" +
                "        }\n" +
                "        \n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public static void main(Integer[] args) {\n" +
                "        System.out.println(\"Method main(Integer[] args) has been invoked\");\n" +
                "\n" +
                "        for (Integer arg : args) {\n" +
                "            System.out.println(arg);\n" +
                "        }\n" +
                "\n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public static void main(double[] args) {\n" +
                "        System.out.println(\"Method main(double[] args) has been invoked\");\n" +
                "\n" +
                "        for (double arg : args) {\n" +
                "            System.out.println(arg);\n" +
                "        }\n" +
                "\n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public void foo(String str, Double d, Integer i) {\n" +
                "        System.out.println(\"Method foo(String str, Double d, Integer i) has been invoked\");\n" +
                "\n" +
                "        System.out.println(\"String: \" + str);\n" +
                "        System.out.println(\"Double: \" + d);\n" +
                "        System.out.println(\"Integer: \" + i);\n" +
                "        \n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public void foo(String str, Long l, Float f) {\n" +
                "        System.out.println(\"Method foo(String str, Long l, Float f) has been invoked\");\n" +
                "\n" +
                "        System.out.println(\"String: \" + str);\n" +
                "        System.out.println(\"Long: \" + l);\n" +
                "        System.out.println(\"Float: \" + f);\n" +
                "\n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public void foo(String str, int i, float f) {\n" +
                "        System.out.println(\"Method foo(String str, int i, float f) has been invoked\");\n" +
                "\n" +
                "        System.out.println(\"String: \" + str);\n" +
                "        System.out.println(\"int: \" + i);\n" +
                "        System.out.println(\"float: \" + f);\n" +
                "        \n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public void foo(String str, Byte b, Float f) {\n" +
                "        System.out.println(\"Method foo(String str, Byte b, Float f) has been invoked\");\n" +
                "\n" +
                "        System.out.println(\"String: \" + str);\n" +
                "        System.out.println(\"Byte: \" + b);\n" +
                "        System.out.println(\"Float: \" + f);\n" +
                "\n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public void foo(String str, short s, float f) {\n" +
                "        System.out.println(\"Method foo(String str, short s, float f) has been invoked\");\n" +
                "\n" +
                "        System.out.println(\"String: \" + str);\n" +
                "        System.out.println(\"short: \" + s);\n" +
                "        System.out.println(\"float: \" + f);\n" +
                "\n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    static public void foo() {\n" +
                "        System.out.println(\"Static method without params has been invoked\");\n" +
                "    }\n" +
                "\n" +
                "    static public void foo(String str, Short s, Integer i) {\n" +
                "        System.out.println(\"Static method foo(String str, Short s, Integer i) has been invoked\");\n" +
                "        System.out.println(\"String: \" + str);\n" +
                "        System.out.println(\"Short: \" + s);\n" +
                "        System.out.println(\"Integer: \" + i);\n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public void foo(boolean[] args) {\n" +
                "        System.out.println(\"Method foo(boolean[] args) has been invoked\");\n" +
                "        for (boolean arg : args) {\n" +
                "            System.out.println(arg);\n" +
                "        }\n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public void foo(int[] args) {\n" +
                "        System.out.println(\"Method foo(int[] args) has been invoked\");\n" +
                "        for (int arg : args) {\n" +
                "            System.out.println(arg);\n" +
                "        }\n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public void foo(short[] args) {\n" +
                "        System.out.println(\"Method foo(short[] args) has been invoked\");\n" +
                "        for (short arg : args) {\n" +
                "            System.out.println(arg);\n" +
                "        }\n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public void foo(Long[] args) {\n" +
                "        System.out.println(\"Method foo(Long[] args) has been invoked\");\n" +
                "        for (Long arg : args) {\n" +
                "            System.out.println(arg);\n" +
                "        }\n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public void foo(Float[] args) {\n" +
                "        System.out.println(\"Method foo(Float[] args) has been invoked\");\n" +
                "        for (Float arg : args) {\n" +
                "            System.out.println(arg);\n" +
                "        }\n" +
                "        System.out.println(\"Done\");\n" +
                "    }\n" +
                "\n" +
                "    public void globalVarInstance() {\n" +
                "        System.out.println(\"intVar value = \" + intVar);\n" +
                "        intVar++;\n" +
                "    }\n" +
                "\n" +
                "    public static void globalVarStatic() {\n" +
                "        System.out.println(\"statIntVar value = \" + statIntVar);\n" +
                "        statIntVar++;\n" +
                "    }\n" +
                "\n" +
                "}\n";

        IProblem[] problemArr = EclipseJdt.checkSyntax(sourceCode);
        assert problemArr.length == 0;
    }
}
