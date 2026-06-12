
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

    public void switchCaseStatementCaseIncludingIf() {
        int num = 2;
        int str;
        switch (num) {
            case 1:
                str = 1;
                if( num == 666){
                    str = 11;
                }else{
                    str = 12;
                }
                break;
            case 2:  str = 2;
                break;
            case 3:  str = 3;
                break;
            default: str = -1;
                break;
        }
    }

    public void switchWithSwitch() {
        int num = 2;
        int str;
        switch (num) {
            case 1:
                switch (num) {
                    case 10:
                        str = 11;
                        break;
                    case 20:
                        str = 12;
                        break;
                }
                break;
            case 2:  str = 2;
                switch (num) {
                    case 20:
                        str = 220;
                        break;
                    case 30:
                        str = 230;
                        break;
                    case 40:
                        str = 240;
                        break;
                }
                break;
            case 3:  str = 3;
                break;
            default: str = -1;
                break;
        }
    }



}
