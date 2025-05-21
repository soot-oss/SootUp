package t2;

public class ThreadRun {
    public static void main(String[] args) {
        StartRunRunnable2 task = new StartRunRunnable2();

        Thread thread = new Thread(task);
        thread.start();
    }
}