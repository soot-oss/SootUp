package sootup.spark;

import java.util.concurrent.atomic.AtomicLong;

public class Engine {
  public boolean ignoreBaseObjects = false; // Field-based vs. field-sensitive
  public boolean parmsAsFields = false; // Represent params as field ref nodes
  public boolean returnsAsFields = false; // Represent returns as field ref nodes

  public boolean typesForSites = false; // Group allocations by run-time type
  public boolean mergeStringBuffer = true; // Group StringBuffer allocations

  public boolean simulateNatives = true; // Model standard library native methods
  public boolean simpleEdgesBidirectional = false; // For unification-based analysis
  public boolean onFlyCallGraph = false; // Build interprocedural edges dynamically

  private static final AtomicLong allocCount = new AtomicLong(0);

  public static long incrementAndGetAllocCount() {
    return allocCount.incrementAndGet();
  }

  public static long getAllocCount() {
    return allocCount.get();
  }

  public static void resetAllocCount() {
    allocCount.set(0);
  }
}
