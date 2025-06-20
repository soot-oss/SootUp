package t3;

class UpdatedThreadInner extends Thread {
    @Override
    public synchronized void start() {
        super.start();
    }
}

class UpdatedThreadOuter extends UpdatedThreadInner {
    @Override
    public void run() {

    }
}

class Example3 {
    public static void main(String[] args) {
        UpdatedThreadOuter thread = new UpdatedThreadOuter();
        thread.start();
    }
}