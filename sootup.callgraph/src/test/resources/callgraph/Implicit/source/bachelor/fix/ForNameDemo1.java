package bachelor.fix;

public class ForNameDemo1 {
    public static void main(String[] args) throws Exception {
        Class.forName("bachelor.fix.Target1");
    }
}

class Target1 {
}
