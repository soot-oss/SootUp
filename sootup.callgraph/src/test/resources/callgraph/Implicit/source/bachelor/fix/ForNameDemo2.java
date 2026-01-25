package bachelor.fix;

public class ForNameDemo2 {
    public static void main(String[] args) throws Exception {
        Class.forName("bachelor.fix.Target2", false, ForNameDemo2.class.getClassLoader());
        Class.forName("bachelor.fix.Target2", true, ForNameDemo2.class.getClassLoader());
    }
}

class Target2 {
}
