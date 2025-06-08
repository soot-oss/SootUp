package t1;

class UpdatedThread1 extends Thread {
    @Override
    public void run() {
        System.out.println("Example1 run() executed.");
    }

    @Override
    public synchronized void start() {
        super.start();
        System.out.println("Example1 start() executed.");
    }
}

class Example1 {
    public static void main(String[] args) {
        UpdatedThread1 thread = new UpdatedThread1();
        thread.start();
    }
}