package de.upb.sootup.instructions.javabytecode.stmt;

public class MonitorStmtsTest {

  public void enterAndExit() {

    StringBuilder sb = new StringBuilder();
    synchronized (sb) {
      sb.append("monitored");
    }
    sb.append("unmonitored");
    System.out.println(sb.toString());

  }

}
