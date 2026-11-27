package ir;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Executor;

class Job implements Runnable {
  public void run() {}
}

class OtherJob implements Runnable {
  public void run() {}

  public void work() {}
}

class Action implements PrivilegedAction<Object> {
  public Object run() {
    return null;
  }
}

/** Starts the thread through super.start() */
class SuperStartThread extends Thread {
  @Override
  public synchronized void start() {
    super.start();
  }

  @Override
  public void run() {}
}

/** Never starts the thread */
class NoStartThread extends Thread {
  @Override
  public synchronized void start() {}

  @Override
  public void run() {}
}

/** Queues nothing and runs nothing */
class DroppingExecutor implements Executor {
  public void execute(Runnable command) {}
}

/** Declares execute(Runnable), but is no Executor */
class Base {
  public void execute(Runnable command) {}
}

/** An Executor whose execute(Runnable) is inherited from Base */
class InheritingExecutor extends Base implements Executor {}

/** Has an execute(Runnable), but is no Executor */
class Unrelated {
  public void execute(Runnable command) {}
}

/** Not in the view: its class file is deleted */
class MissingExecutor {
  public void execute(Runnable command) {}
}

class Util {
  public static void post(Runnable r) {}
}

/** Inherits post(Runnable) */
class AppUtil extends Util {}

/** Hides post(Runnable) with its own, which calls the hidden one */
class HidingUtil extends Util {
  public static void post(Runnable r) {
    Util.post(r);
  }
}

class ImplicitResolution {
  static void superStart() {
    new SuperStartThread().start();
  }

  static void overrideWithoutSuper() {
    new NoStartThread().start();
  }

  static void threadTypedOverride() {
    Thread thread = new NoStartThread();
    thread.start();
  }

  static void droppingExecutor() {
    new DroppingExecutor().execute(new Job());
  }

  static void inheritingExecutor() {
    new InheritingExecutor().execute(new Job());
  }

  static void interfaceTypedExecutor() {
    Executor executor = new DroppingExecutor();
    executor.execute(new Job());
  }

  static void unrelated() {
    new Unrelated().execute(new Job());
  }

  static void missing() {
    new MissingExecutor().execute(new Job());
  }

  static void staticThroughSubclass() {
    AppUtil.post(new Job());
  }

  static void hidingStatic() {
    HidingUtil.post(new Job());
  }

  static void narrowLocal(Executor executor) {
    Job job = new Job();
    executor.execute(job);
  }

  static void wideLocal(Executor executor, Runnable runnable) {
    executor.execute(runnable);
  }

  /** Not called from main: a plain Thread is instantiated only after the call of start() */
  static void lateInstantiation() {
    Thread thread = new NoStartThread();
    thread.start();
    instantiatePlainThread();
  }

  static void instantiatePlainThread() {
    new Thread();
  }

  static void work() {
    Util.post(new OtherJob());
  }

  public static void main(String[] args) {
    new OtherJob();
    AccessController.doPrivileged(new Action());
    superStart();
    overrideWithoutSuper();
    threadTypedOverride();
    droppingExecutor();
    inheritingExecutor();
    interfaceTypedExecutor();
    unrelated();
    missing();
    staticThroughSubclass();
    hidingStatic();
    narrowLocal(new DroppingExecutor());
    wideLocal(new DroppingExecutor(), new Job());
    work();
  }
}
