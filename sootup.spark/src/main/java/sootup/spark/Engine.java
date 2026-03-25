package sootup.spark;

import java.util.concurrent.atomic.AtomicLong;

public class Engine {

  private static final AtomicLong allocCount = new AtomicLong(0);

  public static long incrementAndGetAllocCount() {
    return allocCount.incrementAndGet();
  }

  public static void resetAllocCount() {
    allocCount.set(0);
  }
}
