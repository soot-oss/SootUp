package de.upb.swt.soot.core.util;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Christian Brüggemann, Markus Schmidt
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import de.upb.swt.soot.core.jimple.basic.EquivTo;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.util.printer.Printer;
import de.upb.swt.soot.core.util.printer.Printer.Option;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringEscapeUtils;

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

    return filterJimple(writer.toString());
  }

  @Nonnull
  public static ArrayList<String> filterJimple(String str) {
    return filterJimple(
        Arrays.stream(str.split("\n")).skip(1) // Remove method declaration
        );
  }

  public static ArrayList<String> filterJimple(Stream<String> stream) {
    return stream
        .map(String::trim)
        .map(line -> line.endsWith(";") ? line.substring(0, line.length() - 1) : line)
        .filter(line -> !line.isEmpty() && !"{".equals(line) && !"}".equals(line))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static void printJimpleForTest(SootMethod m) {
    System.out.println(printedJimpleToArrayRepresentation(m.getBody()));
  }

  /** Helper for writing tests . */
  public static String printedJimpleToArrayRepresentation(Body b) {
    ArrayList<String> arr = filterJimple(Utils.bodyStmtsAsStrings(b).stream());
    return printJimpleStmtsForTest(arr);
  }

  public static String printJimpleStmtsForTest(List<String> stmts) {
    StringBuilder sb = new StringBuilder();
    stmts.forEach(
        item ->
            sb.append('"')
                .append(StringEscapeUtils.escapeJava(item))
                .append('"')
                .append(',')
                .append("\n"));
    if (stmts.size() > 0) {
      sb.setLength(sb.length() - 1);
    }
    return sb.toString();
  }
}
