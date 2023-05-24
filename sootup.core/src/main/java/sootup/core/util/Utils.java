package sootup.core.util;

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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.apache.commons.lang3.StringEscapeUtils;
import sootup.core.jimple.basic.EquivTo;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.util.printer.JimplePrinter;

/** @author Linghui Luo */
public class Utils {

  @Nullable
  Path compileJavaOTF(String className, String javaSourceContent) {
    File sourceFile;
    try {
      Path root = Files.createTempDirectory("JavaOTFCompileTempDir");
      root.toFile().deleteOnExit();

      sourceFile = new File(root.toFile(), className + ".java");
      if (!sourceFile.createNewFile()) {
        return null;
      }
      sourceFile.deleteOnExit();

      Path compileUnitPath = sourceFile.toPath();
      Files.write(compileUnitPath, javaSourceContent.getBytes(StandardCharsets.UTF_8));
      return compileJavaOTF(compileUnitPath);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Nullable
  Path compileJavaOTF(Path sourceFile) {
    // Compile source file.
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    final boolean b = 0 == compiler.run(null, null, null, sourceFile.toString());
    if (b) {
      final File file = new File(sourceFile.toString().replace(".java", ".class"));
      file.deleteOnExit();
      return file.toPath();
    }
    return null;
  }

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
        JimplePrinter printer = new JimplePrinter();
        printer.printTo(cl, writer);
        writer.flush();
        writer.close();
      } catch (FileNotFoundException e) {
        // dont throw again - as this is for debug purposes only
        e.printStackTrace();
      }
    }
  }

  public static void print(SootClass cl, boolean print) {
    if (print) {
      PrintWriter writer = new PrintWriter(System.out);
      JimplePrinter printer = new JimplePrinter();
      printer.printTo(cl, writer);
      writer.flush();
      writer.close();
    }
  }

  public static void print(SootMethod method, boolean print) {
    if (print) {
      PrintWriter writer = new PrintWriter(System.out);
      JimplePrinter printer = new JimplePrinter();
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
      JimplePrinter printer = new JimplePrinter();
      printer.setOption(JimplePrinter.Option.OmitLocalsDeclaration);
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

  public static void generateJimpleForTest(@Nonnull SootMethod m) {
    System.out.println(generateJimpleForTest(m.getBody()));
  }

  /** Helper for writing tests . */
  public static String generateJimpleForTest(@Nonnull Body b) {
    ArrayList<String> arr = filterJimple(Utils.bodyStmtsAsStrings(b).stream());
    return generateJimpleTest(arr);
  }

  public static String generateJimpleTest(@Nonnull List<String> stmts) {
    StringBuilder sb = new StringBuilder();

    sb.append(
        "List<String> actualStmts = Utils.bodyStmtsAsStrings( body );\nAssert.assertEquals(\nStream.of(\n");

    stmts.forEach(
        item ->
            sb.append('"')
                .append(StringEscapeUtils.escapeJava(item))
                .append('"')
                .append(',')
                .append("\n"));
    if (stmts.size() > 0) {
      sb.setCharAt(sb.length() - 2, '\n');
    }

    sb.append(").collect(Collectors.toList()), actualStmts);");

    return sb.toString();
  }
}
