public class SubClassShadowing extends SuperClass{

    String info = "sub";

    public void printInfo(String info){

        System.out.println(info);
        System.out.println(this.info);
        System.out.println(super.info);

    }
}