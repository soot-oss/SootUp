public class StaticInnerClass{
    static int num = 20;
    static class InnerClass{
        void method(){
            System.out.println("num = " + num);
        }
    }
    public void staticInnerClass(){
        StaticInnerClass.InnerClass ic=new StaticInnerClass.InnerClass();
        ic.method();
    }
}