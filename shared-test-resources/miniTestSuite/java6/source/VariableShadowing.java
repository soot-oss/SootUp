
/** @author Hasitha Rajapakse */

public class VariableShadowing{
    int num = 5;

    public void variableShadowing(){
        int val = num;
        int num = 10;
    }
}