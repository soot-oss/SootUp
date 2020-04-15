
/** @author Hasitha Rajapakse **/

public class GenericTypeParamOnMethod{
    public <T> void a(T val){
        System.out.println(val);
    }

    public void genericTypeParamOnMethod() {
        a("Hello World");
    }
}