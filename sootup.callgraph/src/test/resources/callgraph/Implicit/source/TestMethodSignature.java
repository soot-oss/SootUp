public class TestMethodSignature implements Runnable {
    @Override
    public void run() {
        System.out.println("run() method is executed");
    }
    public static void main (String[] args) {
        TestMethodSignature task = new TestMethodSignature();
        task.run();
        //Thread t = new Thread(task);
        //t.run();
    }
}