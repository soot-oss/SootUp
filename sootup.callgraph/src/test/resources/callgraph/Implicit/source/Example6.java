package t6;

class NoThread {
    public void run() {
    }
    public synchronized void start() {
    }
}

class Example6 {
    public static void main(String[] args) {
        NoThread thread = new NoThread();
        thread.start();
    }
}