public class SimpleTaint {

    static String k;

    public void entryPoint() {
        String i = "SECRET";
        String j = i;
        k = i;
    }

}