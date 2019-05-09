package de.upb.soot.frontends.java;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.EquivTo;
import de.upb.soot.util.printer.Printer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.function.Consumer;

/** @author Linghui Luo */
public class Utils {

  public static void outputJimple(SootClass cl, boolean print) {
    if (print) {
      File outputDir = new File("jimpleOutput");
      if (!outputDir.exists()) {
        outputDir.mkdir();
      }
      File file = new File(outputDir + File.separator + cl.getName() + ".jimple");
      PrintWriter writer;
      try {
        writer = new PrintWriter(file);
        Printer printer = new Printer();
        printer.printTo(cl, writer);
        writer.flush();
        writer.close();
      } catch (FileNotFoundException e) {
        // Not rethrowing as this is for debug purposes only
        e.printStackTrace();
      }
    }
  }

  public static void print(SootClass cl, boolean print) {
    if (print) {
      PrintWriter writer = new PrintWriter(System.out);
      Printer printer = new Printer();
      printer.printTo(cl, writer);
      writer.flush();
      writer.close();
    }
  }

  public static void print(SootMethod method, boolean print) {
    if (print) {
      PrintWriter writer = new PrintWriter(System.out);
      Printer printer = new Printer();
      printer.printTo(method.getBody(), writer);
      writer.flush();
      writer.close();
    }
  }

  static void assertEquiv(EquivTo expected, EquivTo actual) {
    if (!expected.equivTo(actual)) {
      throw new AssertionError("Expected '" + expected + "', actual is '" + actual + "'");
    }
  }

  static <T> void assertInstanceOfSatisfying(Object actual, Class<T> tClass, Consumer<T> checker) {
    try {
      checker.accept(tClass.cast(actual));
    } catch (ClassCastException e) {
      throw new AssertionError(
          "Expected value of type "
              + tClass
              + (actual != null ? ", got type " + actual.getClass() + " with value " : ", got ")
              + actual);
    }
  }
}
