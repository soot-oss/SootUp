package de.upb.swt.soot.core.util;

import de.upb.swt.soot.core.jimple.basic.EquivTo;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.util.EscapedWriter;
import de.upb.swt.soot.core.util.printer.Printer;
import de.upb.swt.soot.core.util.printer.Printer.Option;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

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

  public static void assertEquiv(EquivTo expected, EquivTo actual) {
    if (!expected.equivTo(actual)) {
      throw new AssertionError("Expected '" + expected + "', actual is '" + actual + "'");
    }
  }

  public static <T> void assertInstanceOfSatisfying(
      Object actual, Class<T> tClass, Consumer<T> checker) {
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

  @Nonnull
  public static ArrayList<String> bodyStmtsAsStrings(@Nonnull Body body) {
    StringWriter writer = new StringWriter();
    try (PrintWriter writerOut = new PrintWriter(new EscapedWriter(writer))) {
      Printer printer = new Printer();
      printer.setOption(Option.OmitLocalsDeclaration);
      printer.printTo(body, writerOut);
    }

    return Arrays.stream(writer.toString().split("\n"))
        .skip(1) // Remove method declaration
        .map(String::trim)
        .map(line -> line.endsWith(";") ? line.substring(0, line.length() - 1) : line)
        .filter(line -> !line.isEmpty() && !"{".equals(line) && !"}".equals(line))
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
