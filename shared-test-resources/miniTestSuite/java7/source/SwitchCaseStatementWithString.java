/** @author Markus Schmidt */

enum Color {
    RED,
    GREEN
}

public class SwitchCaseStatementWithString {
    public void switchCaseStatementString() {
        String key = "something";
        int retVal;
        switch ( key ) {
            case "one":  ;
                retVal = 1;
                break;
            case "two":
                retVal = 2;
                break;
            case "three":
                retVal = 3;
                break;
            default:
                retVal = -1;
        }
    }

    public void switchCaseStatementInt() {
        int num = 2;
        String str;
        switch (num) {
            case 1:
                str = "number 1 detected";
                break;
            case 2:
                str = "number 2 detected";
                break;
            case 3:
                str = "number 3 detected";
                break;
            default:
                str = "invalid number";
                break;
        }
    }

    public void switchCaseStatementEnum() {
        String color = "RED";
        String str = "";
        switch (Color.valueOf(color)) {
            case RED:
                str = "color red detected";
                break;
            case GREEN:
                str = "color green detected";
                break;
            default:
                str = "invalid color";
                break;
        }
    }
}
