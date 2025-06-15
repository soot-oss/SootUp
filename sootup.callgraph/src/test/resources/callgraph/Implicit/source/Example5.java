package t5;

class UpdatedThread2 extends Thread {
    @Override
    public void run() {
        System.out.println("Example5 run() executed.");
    }
}

class Example5 {
    public static void main(String[] args) {
        UpdatedThread2 thread = new UpdatedThread2();
        thread.start();
    }
}