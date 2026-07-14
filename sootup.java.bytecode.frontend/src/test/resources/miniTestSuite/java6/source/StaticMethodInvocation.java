
/** @author Hasitha Rajapakse **/

public class StaticMethodInvocation{
    public static void staticmethod(){
        String str = "Hello World";
    }
    public void staticMethodInvocation(){
        StaticMethodInvocation.staticmethod();
    }
}