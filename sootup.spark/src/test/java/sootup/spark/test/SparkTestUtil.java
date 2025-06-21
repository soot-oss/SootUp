package sootup.spark.test;

import lombok.experimental.UtilityClass;
import org.graph4j.Edge;
import org.graph4j.Graph;
import sootup.core.signatures.PackageName;
import sootup.core.typehierarchy.PAGVisualizer;
import sootup.core.types.ClassType;
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

  public static String vizualizeMehodPAG(Graph<Node, Edge> pag) {
    String dotOutput = PAGVisualizer.visualizeMethodPAG(pag, Node::toString);
    return dotOutput;
  }
}
