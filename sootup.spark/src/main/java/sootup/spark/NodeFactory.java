package sootup.spark;

import lombok.NonNull;
import sootup.core.jimple.basic.Value;
import sootup.spark.node.Node;
import sootup.spark.node.NodeValueVisitor;

import java.util.Optional;

/*
It is responsible for creating a SPARK representative nodes (3 nodes present in SPARK, AllocNode, FieldRef Node, and VarNode)
 */
public class NodeFactory {

  /*
  1. Whenever we encounter a assign statement, we need to create a localVarNode for both the right and left hand operator
   */

    @NonNull
    public static Optional<Node> createNode(@NonNull Value value){
        NodeValueVisitor visitor = new NodeValueVisitor();
        value.accept(visitor);
        return visitor.get();
    }

}
