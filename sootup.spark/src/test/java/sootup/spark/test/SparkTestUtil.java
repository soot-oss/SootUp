package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import lombok.experimental.UtilityClass;
import org.graph4j.Graph;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.spark.PAGEdge;
import sootup.spark.PAGVisualizer;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.Node;
import sootup.spark.node.VariableNode;

@UtilityClass
public class SparkTestUtil {

  public static final JavaIdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
  public static final JavaView view =
      new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/pta/binary"));
  public static final MethodSignature GLOBAL_SCOPE =
      new MethodSignature(
          new JavaClassType("GLOBAL", new PackageName("sootup.global")),
          "GLOBAL_SCOPE",
          Collections.emptyList(),
          VoidType.getInstance());

  public static ClassType simpleType(String name) {
    return new ClassType() {
      @Override
      public String getFullyQualifiedName() {
        return name;
      }

      @Override
      public String getClassName() {
        return name;
      }

      @Override
      public PackageName getPackageName() {
        return new PackageName("");
      }
    };
  }

  public static Graph<Node, PAGEdge> solveMain(MethodSignature mainSig) {
    return solveMainWithSpark(mainSig).getPag().getDelegate();
  }

  public static Spark solveMainWithSpark(MethodSignature mainSig) {
    assertTrue(view.getClass(mainSig.getDeclClassType()).isPresent());
    assertTrue(view.getMethod(mainSig).isPresent());
    Spark spark =
        Spark.builder().view(view).entryPoints(Collections.singletonList(mainSig)).build();
    spark.solve();
    return spark;
  }

  public static Graph<Node, PAGEdge> solveMain(MethodSignature mainSig, SparkOptions options) {
    assertTrue(view.getClass(mainSig.getDeclClassType()).isPresent());
    assertTrue(view.getMethod(mainSig).isPresent());
    Spark spark =
        Spark.builder()
            .view(view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(options)
            .build();
    spark.solve();
    return spark.getPag().getDelegate();
  }

  public static AllocationNode alloc(ClassType type, long site, MethodSignature sig) {
    return AllocationNode.builder()
        .type(type)
        .allocationSite(site)
        .containingMethodSig(sig)
        .build();
  }

  public static VariableNode var(ClassType type, String name, MethodSignature sig) {
    return VariableNode.builder().type(type).name(name).containingMethodSig(sig).build();
  }

  public static InstanceFieldRefNode fieldRef(
      VariableNode base, FieldSignature field, ClassType fieldType, MethodSignature sig) {
    return InstanceFieldRefNode.builder()
        .base(base)
        .field(field)
        .type(fieldType)
        .containingMethodSig(sig)
        .build();
  }

  public static boolean containsEdge(Graph<Node, PAGEdge> g, Node src, Node tgt) {
    int s = g.findVertex(src);
    int t = g.findVertex(tgt);
    return s >= 0 && t >= 0 && g.containsEdge(s, t);
  }

  public static boolean containsVertex(Graph<Node, PAGEdge> g, Node node) {
    return g.findVertex(node) >= 0;
  }

  public static void vizualizeMehodPAG(Graph<Node, PAGEdge> pag) {
    System.out.println(PAGVisualizer.visualizeMethodPAG(pag, Node::toString));
  }
}
