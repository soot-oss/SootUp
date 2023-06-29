package sootup.analysis.interprocedural.ifds;


import heros.InterproceduralCFG;
import org.junit.Test;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

import static junit.framework.TestCase.assertEquals;

public class ICFGCallGraphTest extends IFDSTaintTestSetUp{

    @Test
    public void ICFGDotExportTest() {
        JavaProject javaProject =
                JavaProject.builder(new JavaLanguage(8))
                        .addInputLocation(
                                new JavaClassPathAnalysisInputLocation(
                                        System.getProperty("java.home") + "/lib/rt.jar"))
                        .addInputLocation(
                                new JavaClassPathAnalysisInputLocation("src/test/resources/taint/binary"))
                        .build();

        view = javaProject.createView();

        JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
        JavaClassType mainClassSignature = identifierFactory.getClassType("ICFGExample");

        SootClass<?> sc = view.getClass(mainClassSignature).get();
        entryMethod =
                sc.getMethods().stream().filter(e -> e.getName().equals("entryPoint")).findFirst().get();

        entryMethodSignature = entryMethod.getSignature();

        JimpleBasedInterproceduralCFG icfg =
                new JimpleBasedInterproceduralCFG(view, entryMethodSignature, false, false);
        String expectedCallGraph = "digraph G {\n" +
                "\tcompound=true\n" +
                "\tlabelloc=b\n" +
                "\tstyle=filled\n" +
                "\tcolor=gray90\n" +
                "\tnode [shape=box,style=filled,color=white]\n" +
                "\tedge [fontsize=10,arrowsize=1.5,fontcolor=grey40]\n" +
                "\tfontsize=10\n" +
                "\n" +
                "//  lines [23: 26] \n" +
                "\tsubgraph cluster_1320677379 { \n" +
                "\t\tlabel = \"Block #1\"\n" +
                "\t\t171497379[label=\"l0 := @this: ICFGExample\",shape=Mdiamond,color=grey50,fillcolor=white]\n" +
                "\t\t1665404403[label=\"l1 = &quot;SECRET&quot;\"]\n" +
                "\t\t988458918[label=\"l2 = virtualinvoke l0.&lt;ICFGExample: java.lang.String id(java.lang.String)&gt;(l1)\"]\n" +
                "\t\t1990451863[label=\"virtualinvoke l0.&lt;ICFGExample: void sink(java.lang.String)&gt;(l2)\"]\n" +
                "\t\t1295083508[label=\"return\",shape=Mdiamond,color=grey50,fillcolor=white]\n" +
                "\n" +
                "\t\t171497379 -> 1665404403 -> 988458918 -> 1990451863 -> 1295083508\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "//  lines [11: 12] \n" +
                "\tsubgraph cluster_2137366542 { \n" +
                "\t\tlabel = \"Block #1\"\n" +
                "\t\t2018981974[label=\"l0 := @this: ICFGExample\",shape=Mdiamond,color=grey50,fillcolor=white]\n" +
                "\t\t905009026[label=\"l1 := @parameter0: java.lang.String\"]\n" +
                "\t\t602908276[label=\"virtualinvoke l0.&lt;ICFGExample: void secondMethod(java.lang.String)&gt;(l1)\"]\n" +
                "\t\t1648078807[label=\"return\",shape=Mdiamond,color=grey50,fillcolor=white]\n" +
                "\n" +
                "\t\t1990451863 -> 2018981974 -> 905009026 -> 602908276 -> 1648078807\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "//  lines [7: 7] \n" +
                "\tsubgraph cluster_603702962 { \n" +
                "\t\tlabel = \"Block #1\"\n" +
                "\t\t1698148964[label=\"l0 := @this: ICFGExample\",shape=Mdiamond,color=grey50,fillcolor=white]\n" +
                "\t\t1288983035[label=\"l1 := @parameter0: java.lang.String\"]\n" +
                "\t\t2064117540[label=\"return l1\",shape=Mdiamond,color=grey50,fillcolor=white]\n" +
                "\n" +
                "\t\t988458918 -> 1698148964 -> 1288983035 -> 2064117540\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "//  lines [15: 16] \n" +
                "\tsubgraph cluster_2116251487 { \n" +
                "\t\tlabel = \"Block #1\"\n" +
                "\t\t329293111[label=\"l0 := @this: ICFGExample\",shape=Mdiamond,color=grey50,fillcolor=white]\n" +
                "\t\t1770804671[label=\"l1 := @parameter0: java.lang.String\"]\n" +
                "\t\t1172514862[label=\"$stack2 = virtualinvoke l0.&lt;ICFGExample: java.lang.String thirdMethod(java.lang.String)&gt;(l1)\"]\n" +
                "\t\t1388137600[label=\"return\",shape=Mdiamond,color=grey50,fillcolor=white]\n" +
                "\n" +
                "\t\t602908276 -> 329293111 -> 1770804671 -> 1172514862 -> 1388137600\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "//  lines [19: 19] \n" +
                "\tsubgraph cluster_1780885275 { \n" +
                "\t\tlabel = \"Block #1\"\n" +
                "\t\t2058348472[label=\"l0 := @this: ICFGExample\",shape=Mdiamond,color=grey50,fillcolor=white]\n" +
                "\t\t2126618194[label=\"l1 := @parameter0: java.lang.String\"]\n" +
                "\t\t1865523042[label=\"return l1\",shape=Mdiamond,color=grey50,fillcolor=white]\n" +
                "\n" +
                "\t\t1172514862 -> 2058348472 -> 2126618194 -> 1865523042\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "}";
        assertEquals(expectedCallGraph,icfg.buildICFGGraph());
    }

}
