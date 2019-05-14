package de.upb.soot.example;

import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.CallGraph;
import de.upb.soot.namespaces.JavaClassPathNamespace;
import de.upb.soot.namespaces.JavaSourcePathNamespace;
import de.upb.soot.namespaces.SourceLocation;
import de.upb.soot.typehierarchy.TypeHierarchy;
import de.upb.soot.views.IView;
import java.util.Collections;

/**
 * A sample application to illustrate a potential client's path through the API
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public class SimpleSootClient {

  public static void main(String[] args) {
    String javaClassPath = "example/classes/";
    String javaSourcePath = "example/src";

    SourceLocation cpBased = new JavaClassPathNamespace(javaClassPath);

    SourceLocation walaSource = new JavaSourcePathNamespace(Collections.singleton(javaSourcePath));

    Project p = new Project(walaSource);

    // 1. simple case
    IView fullView = p.createFullView();

    CallGraph cg = fullView.createCallGraph();
    TypeHierarchy t = fullView.createTypeHierarchy();

    // here goes my own analysis

    // 2. advanced case
    Scope s = new Scope(cpBased);
    IView limitedView = p.createView(s);

    cg = limitedView.createCallGraph();
    t = limitedView.createTypeHierarchy();

    // here goes my own analysis
  }
}
