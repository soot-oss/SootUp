public class FunctionTaintPropagated {

    private String id(String s){
        return s;
    }

    private void sink(String s){

    }

    public void entryPoint() {
        String i = "SECRET";
        String j = id(i);
        sink(j);
    }
}
