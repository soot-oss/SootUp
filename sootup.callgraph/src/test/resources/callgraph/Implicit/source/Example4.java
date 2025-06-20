package t4;

class UpdatedThreadInner extends Thread {
    @Override
    public void run() {

    }
}

class UpdatedThreadOuter extends UpdatedThreadInner {
    @Override
    public synchronized void start() {
        super.start();
    }
}

class Example4 {
    public static void main(String[] args) {
        UpdatedThreadInner thread = new UpdatedThreadInner();
        thread.start();
    }
}