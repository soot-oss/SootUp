package de.upb.soot.example;

import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.JavaClassPathNamespace;
import de.upb.soot.namespaces.JavaSourcePathNamespace;
import de.upb.soot.typehierarchy.ITypeHierarchy;
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

    INamespace cpBased = new JavaClassPathNamespace(javaClassPath);

    INamespace walaSource = new JavaSourcePathNamespace(Collections.singleton(javaSourcePath));

    Project p = new Project(walaSource);

    // 1. simple case
    IView fullView = p.createFullView();

    ICallGraph cg = fullView.createCallGraph();
    ITypeHierarchy t = fullView.createTypeHierarchy();

    // here goes my own analysis

    // 2. advanced case
    Scope s = new Scope(cpBased);
    IView limitedView = p.createView(s);

    cg = limitedView.createCallGraph();
    t = limitedView.createTypeHierarchy();

    // here goes my own analysis
  }
}
