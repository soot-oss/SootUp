public class PublicClass {
    public int a = 10;
    private int b = 20;
    protected int c = 30;
    int d = 40;

    public void publicMethod() {
        a = 20;
        b = 30;
        c = 40;
        d = 50;
    }

    private void privateMethod() {
        a = 20;
        b = 30;
        c = 40;
        d = 50;
    }

    protected void protectedMethod() {
        a = 20;
        b = 30;
        c = 40;
        d = 50;
    }

    void noModifierMethod() {
        a = 20;
        b = 30;
        c = 40;
        d = 50;
    }
}
