public class NestedClassShadow{

    String info = "outer";
    NestedClass clazz = new NestedClass();

    public void printInfo(String info){
        clazz.printInfo(info);
    }

    public class NestedClass{

        String info = "inner";

        public void printInfo(String info){
            System.out.println(info);
            System.out.println(this.info);
            System.out.println(NestedClassShadow.this.info);
        }
    }
}