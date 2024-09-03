public class FunctionTaint {

    private String source(){
        return "SECRET";
    }

    private void sink(String s){

    }

    public void entryPoint() {
        String i = source();
        String j = i;
        sink(j);
    }
}
