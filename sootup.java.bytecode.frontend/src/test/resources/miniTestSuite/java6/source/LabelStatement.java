
/** @author Hasitha Rajapakse */


public class LabelStatement{
    public void labelStatement(){
        int num = 20;
        int i = 1;
        start:
            while (i<num){
                if ( i % 10 == 0 )
                    break start;
                else
                    i++;
        }
    }
}