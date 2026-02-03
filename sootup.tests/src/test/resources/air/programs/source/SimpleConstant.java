public class SimpleConstant {
    int a = 5;
    int b = 5;
    public static int main(String[] args) {
        int c = 0;
        SimpleConstant simpleConstant = new SimpleConstant();
        int loadVariable = simpleConstant.a;
        int storeVariable = simpleConstant.b = 10;
        return c;
    }
}