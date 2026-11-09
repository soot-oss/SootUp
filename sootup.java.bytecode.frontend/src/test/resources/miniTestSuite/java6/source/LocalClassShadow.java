public class LocalClassShadow{
    String info = "outer";

    public void printInfo(String info){

        class LocalClass{
            private String info = "local";

            public void printInfo(String info){
                System.out.println(info);
                System.out.println(this.info);
                System.out.println(LocalClassShadow.this.info);
            }
        }
        LocalClass lc = new LocalClass();
        lc.printInfo(info);
    }
}