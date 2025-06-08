package sootup.callgraph;

class UpdatedThread extends Thread {
    @Override
    public void run() {
        System.out.println("TestTestTest");
        System.out.println("Running Thread: " + Thread.currentThread().getName());
    }

    @Override
    public synchronized void start() {
        super.start();
        System.out.println("Starting Thread: " + Thread.currentThread().getName());
    }
}

public class OverwrittenRun2 {
    public static void main(String[] args) {
        UpdatedThread thread = new UpdatedThread();
        thread.start();
    }
}
