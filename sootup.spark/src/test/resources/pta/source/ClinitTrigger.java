public class ClinitTrigger {

    public static void main(String[] args) {
        int a = Triggered.value;
    }

    static class Triggered {
        static int value = 3;
    }
}
