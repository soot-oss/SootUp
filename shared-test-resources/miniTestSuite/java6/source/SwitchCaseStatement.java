
/** @author Hasitha Rajapakse */


public class SwitchCaseStatement {
    public enum Color{
        RED, GREEN
    }

    public void switchCaseStatementEnum() {
        Color color = Color.RED;
        String str = "";
        switch (color){
            case RED:
                str = "red";
                break;
            case GREEN:
                str = "green";
                break;
            default:
                str = "invalid";
                break;
        }
    }

    public void switchCaseStatementInt() {
        int num = 5;
        String str;
        switch (num) {
            case 1:  str = "one";
                break;
            case 2:  str = "two";
                break;
            case 3:  str = "three";
                break;
            default: str = "invalid";
                break;
        }
    }

    public void switchCaseWithoutDefault() {
        int num = 6;
        String str;
        switch (num) {
            case 1:  str = "one";
                break;
            case 2:  str = "two";
                break;
            case 3:  str = "three";
                break;
        }
    }

    public void switchCaseGroupedTargets() {
        int num = 7;
        String str;
        switch (num) {
            case 1:
            case 2:
                str = "first";
                break;
            case 3:
                str = "second";
                break;
        }
    }

    public void switchCaseGroupedTargetsDefault() {
        int num = 8;
        String str;
        switch (num) {
            case 1:
            case 2:
                str = "first";
                break;
            case 3:
                str = "second";
                break;
            default:
                str = "other";
        }
    }

}
