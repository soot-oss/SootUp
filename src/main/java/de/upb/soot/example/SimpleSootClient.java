package de.upb.soot.example;

import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.namespaces.JarFileNamespace;
import de.upb.soot.views.IView;
import de.upb.soot.typehierarchy.ITypeHierarchy;

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

        Project p = new Project(new JarFileNamespace(jarFile));

        // simple case
        IView v = p.createFullView();

        ICallGraph cg = v.createCallGraph();
        ITypeHierarchy t = v.createTypeHierarchy();

        // here goes my own analysis

        // advanced case
        Scope s = new Scope();
        // TODO add scoping
        IView limitedView = p.createView(s);

    }

}
