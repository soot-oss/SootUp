import java.io.Serializable;;

class TransientVariable {

    transient int transientVar= 1;

    public void transientVariable(){
        System.out.println(transientVar);
    }
}
