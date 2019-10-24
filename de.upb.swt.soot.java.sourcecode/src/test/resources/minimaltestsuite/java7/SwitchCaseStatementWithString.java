/** @author: Markus Schmidt */

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
}
