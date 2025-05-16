class UpdatedThread extends Thread {
    public UpdatedThread(Runnable target) {
        super(target);
    }

    @Override
    public void run() {
        super.run();
        System.out.println("Running Thread: " + Thread.currentThread().getName());
    }

    @Override
    public synchronized void start() {
        super.start();
        System.out.println("Starting Thread: " + Thread.currentThread().getName());
    }
}

public class Runner {
    public static void main(String[] args) {
        ImplicitRunStartRunnable task = new ImplicitRunStartRunnable();

        UpdatedThread thread = new UpdatedThread(task);
        thread.start();
    }
}