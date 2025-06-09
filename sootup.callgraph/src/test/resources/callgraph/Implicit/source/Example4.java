package t4;

class UpdatedThreadInner extends Thread {
    @Override
    public void run() {
        System.out.println("Example4 run() executed.");
    }
}

class UpdatedThreadOuter extends UpdatedThreadInner {
    @Override
    public synchronized void start() {
        super.start();
        System.out.println("Example4 start() executed.");
    }
}

class Example4 {
    public static void main(String[] args) {
        UpdatedThreadInner thread = new UpdatedThreadInner();
        thread.start();
    }
}