package sootup.tests; /*-
                       * #%L
                       * Soot - a J*va Optimization Framework
                       * %%
                       * Copyright (C) 2018-2020 Linghui Luo, Ben Hermann, Markus Schmidt and others
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

import com.ibm.wala.ipa.callgraph.CallGraph;
import java.util.Collections;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

/**
 * A sample application to illustrate a potential client's path through the API
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public class SimpleSootClient {

  public static void main(String[] args) {
    String javaClassPath = "de/upb/soot/example/classes/";
    String javaSourcePath = "de/upb/soot/example/src";

    AnalysisInputLocation<JavaSootClass> cpBased =
        new JavaClassPathAnalysisInputLocation(javaClassPath);

    AnalysisInputLocation<JavaSootClass> walaSource =
        new JavaSourcePathAnalysisInputLocation(Collections.singleton(javaSourcePath));

    JavaProject p = JavaProject.builder(new JavaLanguage(8)).addInputLocation(walaSource).build();

    // 1. simple case
    JavaView fullView = p.createView();

    /*
        CallGraph cg = fullView.createCallGraph();
        TypeHierarchy t = fullView.typeHierarchy();
        mySootAnalysis( cg );


        // 2. advanced case with scope
        Scope s = new Scope(cpBased);
        View limitedView = p.createView(s);
        cg = limitedView.createCallGraph();
        t = limitedView.typeHierarchy();
        mySootAnalysis( cg );
    */

  }

  /** dummy method */
  static void mySootAnalysis(CallGraph cg) {
    // here goes my own analysis
  }
}
