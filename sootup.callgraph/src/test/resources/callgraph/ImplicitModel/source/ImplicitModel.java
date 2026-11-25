package im;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Executor;

class MyThread extends Thread {
  @Override
  public void run() {}
}

class ThreadRunnable implements Runnable {
  @Override
  public void run() {}
}

class Task implements Runnable {
  @Override
  public void run() {}
}

class DirectExecutor implements Executor {
  @Override
  public void execute(Runnable command) {}
}

class Action implements PrivilegedAction<Object> {
  @Override
  public Object run() {
    return null;
  }
}

class ImplicitModel {
  public static void main(String[] args) {
    // a subclass of Thread, referenced through its own type
    MyThread myThread = new MyThread();
    myThread.start();

    // a Runnable passed to the constructor of Thread
    Thread thread = new Thread(new ThreadRunnable());
    thread.start();

    // a Runnable passed to an Executor
    Executor executor = new DirectExecutor();
    executor.execute(new Task());

    // a PrivilegedAction passed to a static method
    AccessController.doPrivileged(new Action());
  }
}
