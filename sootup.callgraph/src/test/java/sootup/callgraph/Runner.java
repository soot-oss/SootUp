package sootup.callgraph;

public class Runner {
    public static void main(String[] args) {
        ImplicitRunStartRunnable task = new ImplicitRunStartRunnable();

        Thread thread = new Thread(task);
        thread.start();
    }
}