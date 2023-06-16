
public class ICFGExample {
    public ICFGExample() {
    }

    private String id(String var1) {
        return var1;
    }

    private void sink(String var1) {
        secondMethod(var1);
    }

    private void secondMethod(String var1){
        thirdMethod(var1);
    }

    public String thirdMethod(String var1){
        return var1;
    }

    public void entryPoint() {
        String var1 = "SECRET";
        String var2 = this.id(var1);
        this.sink(var2);
    }
}