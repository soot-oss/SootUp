package sootup.spark.test;

import lombok.val;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTExporter;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.spark.node.Node;

import java.io.StringWriter;

public class SparkTestUtil {

    public static ClassType simpleType(String name){
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

    public static void vizualizeMehodPAG(Graph<Node, DefaultEdge> pag){
        val exporter = new DOTExporter<Node, DefaultEdge>(
                v -> v.toString()
        );
        StringWriter writer = new StringWriter();
        exporter.exportGraph(pag, writer);
        System.out.println(writer);
    }

}
