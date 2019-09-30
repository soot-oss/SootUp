package de.upb.soot.java6;

public class ContinueInWhileLoop {

    public int continueInWhileLoop(int num1, int num2){
        int total = 0;
        while (num1 > num2){
            if(num1==0) {
                continue;
            }
            total += num1;
            num1--;
            System.out.println("Current total is = " + total);
        }
        return total;
    }
}
