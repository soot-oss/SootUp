package de.upb.soot.javasourcecodefrontend.example;

/**
 * A sample application to illustrate a potential client's path through the API
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public class SimpleSootClient {

  // TODO: refactor to integration tests

  /*
  public static void main(String[] args) {
    String javaClassPath = "de/upb/soot/example/classes/";
    String javaSourcePath = "de/upb/soot/example/src";

    AnalysisInputLocation cpBased = new JavaClassPathAnalysisInputLocation(javaClassPath);

    AnalysisInputLocation walaSource =
        new JavaSourcePathAnalysisInputLocation(Collections.singleton(javaSourcePath));

    Project<AnalysisInputLocation> p = new Project<>(walaSource);

    // 1. simple case
    View fullView = p.createFullView();

    CallGraph cg = fullView.createCallGraph();
    TypeHierarchy t = fullView.typeHierarchy();

    // here goes my own analysis

    // 2. advanced case
    Scope s = new Scope(cpBased);
    View limitedView = p.createView(s);

    cg = limitedView.createCallGraph();
    t = limitedView.typeHierarchy();

    // here goes my own analysis
  }
   */
}
