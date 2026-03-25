package sootup.spark.test;

import java.io.StringWriter;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jgrapht.Graph;
import org.jgrapht.nio.dot.DOTExporter;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.spark.PAGEdge;
import sootup.spark.node.Node;

@UtilityClass
public class SparkTestUtil {

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

  public static void vizualizeMehodPAG(Graph<Node, PAGEdge> pag) {
    val exporter = new DOTExporter<Node, PAGEdge>(Node::toString);
    StringWriter writer = new StringWriter();
    exporter.exportGraph(pag, writer);
    System.out.println(writer);
  }
}
