package de.upb.soot.example;

import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.JarFileNamespace;
import de.upb.soot.namespaces.JavaClassPathNamespace;
import de.upb.soot.typehierarchy.ITypeHierarchy;
import de.upb.soot.views.IView;

import java.io.File;

/**
 * A sample application to illustrate a potential client's path through the API
 *
 * @author Linghui Luo
 * @author Ben Hermann
 *
 */
public class SimpleSootClient {

  public static void main(String[] args) {
    String jarFileName = "test.jar";
    File jarFile = new File(jarFileName);
    
    String javaClassPath = "exmaple//classes//";
    
    INamespace np1=new JarFileNamespace(jarFile);
    INamespace np2=new JavaClassPathNamespace(javaClassPath);

    Project p = new Project(np1, np2);

    // 1. simple case
    IView fullView = p.createFullView();

    ICallGraph cg = fullView.createCallGraph();
    ITypeHierarchy t = fullView.createTypeHierarchy();

    // here goes my own analysis

    // 2. advanced case
    Scope s = new Scope(np1);
    IView limitedView = p.createView(s);

    cg = limitedView.createCallGraph();
    t = limitedView.createTypeHierarchy();

    // here goes my own analysis
  }

}
