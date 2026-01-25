package bachelor.fix;

public class RuntimeExit {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Target Reached: Shutdown hook executed.");
        }));
        Runtime.getRuntime().exit(0);
    }
}