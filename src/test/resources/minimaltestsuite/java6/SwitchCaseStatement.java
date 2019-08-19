public class SwitchCaseStatement {

    public void switchCaseStatement() {
        String color = "red";
        String str = "";
        switch (color){
            case "red":
                str = "color red detected";
                break;
            case "green":
                str = "color green detected";
                break;
            default:
                str = "invalid color";
        }
    }
}
