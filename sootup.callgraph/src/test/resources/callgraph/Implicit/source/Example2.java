package t2;

class UpdatedThreadInner extends Thread {
    @Override
    public void run() {}
}

class UpdatedThreadOuter extends UpdatedThreadInner {
    @Override
    public synchronized void start() {
        super.start();
    }
}

class Example2 {
    public static void main(String[] args) {
        UpdatedThreadOuter thread = new UpdatedThreadOuter();
        thread.start();
    }
}