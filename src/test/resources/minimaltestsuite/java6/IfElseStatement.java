package de.upb.soot.java6;

public class IfElseStatement {

    public String ifElseStatement(int a, int b, int c){
        String str;

        if(a < b){
            str = "if statement";
        }else if( b < c){
            str = "else if statement";
        }else{
            str="else statement";
        }
        return str;
    }
}
