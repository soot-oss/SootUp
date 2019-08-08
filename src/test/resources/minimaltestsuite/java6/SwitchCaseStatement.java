package de.upb.soot.java6;

public class SwitchCaseStatement {

    public String switchCaseStatement(String color) {
        String str;
        switch (color){
            case "red":
                str = "color red detected";
                break;
            case "green":
                str = "color green detected";
                break;
            default:
                str = "invalid color";
                break;
        }
        return str;

    }
}
