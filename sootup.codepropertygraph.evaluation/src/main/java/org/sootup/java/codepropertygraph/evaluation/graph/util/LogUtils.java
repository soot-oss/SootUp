package org.sootup.java.codepropertygraph.evaluation.graph.util;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class LogUtils {
  public static void setFileSysOut(String logFilePath) {
    try {
      System.setOut(new PrintStream(logFilePath));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
