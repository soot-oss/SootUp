package sootup.callgraph;

public class ThreadRunTest {
    public static void main(String[] args) {
        StartRunRunnable2Test task = new StartRunRunnable2Test();

        Thread thread = new Thread(task);
        thread.start();
    }
}
