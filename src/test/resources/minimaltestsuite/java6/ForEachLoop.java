package de.upb.soot.java6;

public class ForEachLoop {


    public int forEachLoop(int[] numArray){
        int count = 0;

        for (int item :numArray
             ) {
            count++;
        }
        return count;
    }
}
