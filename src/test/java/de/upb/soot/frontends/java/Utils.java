package de.upb.soot.frontends.java;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.util.printer.Printer;
import java.io.PrintWriter;

/** @author Linghui Luo */
public class Utils {

  public static void print(SootClass cl, boolean print) {
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(cl, writer);
    if (print) {
      writer.flush();
      writer.close();
    }
  }

  public static void print(SootMethod method, boolean flush) {
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    if (flush) {
      writer.flush();
      writer.close();
    }
  }
}
