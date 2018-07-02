package de.upb.soot.LambdaExpressions;

public class LambdaExpressions {
    public static void main(String args[]) {
        A a1 = (str) -> "Hello " + str + "!";
        A a2 = (str) -> "Hello " + str + "!";

        System.out.println(a1.methodA("WORLD"));
        System.out.println(a2.methodA("world"));
    }
}
