package t1;

class UpdatedThread1 extends Thread {
    @Override
    public void run() {
    }

    @Override
    public synchronized void start() {
        super.start();
    }
}

class Example1 {
    public static void main(String[] args) {
        UpdatedThread1 thread = new UpdatedThread1();
        thread.start();
    }
}