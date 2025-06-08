package sootup.callgraph;

class UpdatedThreadInner extends Thread {
//    @Override
//    public void run() {
//        System.out.println("TestExample2 run() executed.");
//    }
    @Override
    public synchronized void start() {
        super.start();
        System.out.println("TestExample2 start() executed.");
    }
}

class UpdatedThreadOuter extends UpdatedThreadInner {
//    @Override
//    public synchronized void start() {
//        super.start();
//        System.out.println("TestExample2 start() executed.");
//    }
    @Override
    public void run() {
        System.out.println("TestExample2 run() executed.");
    }
}

class TestExample2 {
    public static void main(String[] args) {
        UpdatedThreadOuter thread = new UpdatedThreadOuter();
        thread.start();
    }
}
