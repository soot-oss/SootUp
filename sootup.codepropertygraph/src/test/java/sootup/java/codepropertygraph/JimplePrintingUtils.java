package sootup.java.codepropertygraph;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.util.printer.JimplePrinter;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

public class JimplePrintingUtils {
  public void printJimple(String path) {
    List<AnalysisInputLocation<? extends JavaSootClass>> inputLocations = new ArrayList<>();
    inputLocations.add(new JavaClassPathAnalysisInputLocation(path));
    JavaView view = new JavaView(inputLocations);

    JimplePrinter jimplePrinter = new JimplePrinter();
    PrintWriter writer = new PrintWriter(System.out);

    for (JavaSootClass sootClass : view.getClasses()) {
      jimplePrinter.printTo(sootClass, writer);
    }

    writer.flush();
    writer.close();
  }
}
