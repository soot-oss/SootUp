package bachelor.fix;

public class SystemExit {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Target Reached: Shutdown hook executed.");
        }));
        System.exit(0);
    }
}