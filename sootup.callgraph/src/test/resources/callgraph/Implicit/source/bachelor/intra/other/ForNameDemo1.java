package bachelor.intra.other;

public class ForNameDemo1 {
    public static void main(String[] args) throws Exception {
        Class.forName("bachelor.intra.other.Target1");
    }
}

class Target1 {
}
