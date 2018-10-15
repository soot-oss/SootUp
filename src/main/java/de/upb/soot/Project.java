package de.upb.soot;

import de.upb.soot.buildactor.ViewBuilder;
import de.upb.soot.namespaces.CompositeNamespace;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.JavaClassPathNamespace;
import de.upb.soot.util.NotYetImplementedException;
import de.upb.soot.views.IView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Soot user should first define a Project instance to describe the outlines of an analysis run.
 * It is the starting point for all operations.
 * You can have multiple instances of projects as there is no information shared between them.
 * All caches are always at the project level.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public class Project {
    private List<INamespace> namespaces = new ArrayList<>();

    /**
     * Create a project from an arbitrary list of namespaces
     * @param namespaces
     */
    public Project(INamespace... namespaces) {
        Collections.addAll(this.namespaces, namespaces);
    }

    /**
     * Create a project from a single JAR file
     * @param file
     */
    public Project(File file) {
        this.namespaces.add(new JavaClassPathNamespace(file.getAbsolutePath()));
    }


    /**
     * Create a complete view from everything in all provided namespaces.
     * This method starts the reification process.
     *
     * @return A complete view on the provided code
     */
    public IView createFullView() {
        CompositeNamespace cn = new CompositeNamespace(this.namespaces);
        ViewBuilder vb = new ViewBuilder(cn);
        return vb.buildComplete();
    }


    /**
     * Returns a partial view on the code based on the provided scope and all namespaces in the project.
     * This method starts the reification process.
     *
     * @param s A scope of interest for the view
     * @return A scoped view of the provided code
     */
    public IView createView(Scope s) {
        throw new NotYetImplementedException(); // TODO
    }

}
