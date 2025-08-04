public class FunctionTaintSanitized {

    private String sanitize(String s){
        return "";
    }

    private void sink(String s){

    }

    public void entryPoint() {
        String i = "SECRET";
        String j = sanitize(i);
        sink(j);
    }
}
