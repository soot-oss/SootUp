public class StaticInitializer{
    static int i=5;

    static{
        if(i>4)
        {
            i=4;
        }
    }

    static methodStaticInitializer(){
        System.out.println(i);
    }
}
