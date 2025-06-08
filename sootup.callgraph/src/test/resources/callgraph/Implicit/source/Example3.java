package t3;

class UpdatedThreadInner extends Thread {
    @Override
    public synchronized void start() {
        super.start();
        System.out.println("Example2 start() executed.");
    }
}

class UpdatedThreadOuter extends UpdatedThreadInner {
    @Override
    public void run() {
        System.out.println("Example2 run() executed.");
    }
}

class Example3 {
    public static void main(String[] args) {
        UpdatedThreadOuter thread = new UpdatedThreadOuter();
        thread.start();
    }
}