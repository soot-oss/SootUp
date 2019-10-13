
/** @author: Hasitha Rajapakse */


public class SwitchCaseStatement {
    public enum Color{
        RED, GREEN
    }

    public String switchCaseStatement(String color) {
        String str = "";
        switch (Color.valueOf(color)){
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
        return str;
    }
}
