public class Runner {
    public void executeRunnable(ImplicitRunStartRunnable r) {
        System.out.println("Calling run() directly...");
        r.run();
    }

    public static void main(String[] args) {
        ImplicitRunStartRunnable myTask = new ImplicitRunStartRunnable();
        Runner runner = new Runner();
        runner.executeRunnable(myTask);
    }
}
