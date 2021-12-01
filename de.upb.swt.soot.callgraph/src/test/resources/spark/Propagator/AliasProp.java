import objects.*;

public class AliasProp {

    public static void main(String[] args) {

        O a = new O();
        O b = new O();
        F c = new F();

        O p, q;
        if(Math.random() < 1){
            p = a;
        }else{
            p = b;
        }
        q = a;
        p.f = c;
        F s = q.f;
    }
}
