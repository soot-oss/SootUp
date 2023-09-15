package qilin.pta.toolkits.debloaterx;

import qilin.core.pag.Node;
import soot.jimple.spark.pag.SparkField;

public class Edge {
    Node from;
    Node to;
    SparkField field;
    EdgeKind kind;

    Edge(Node from, Node to, SparkField f, EdgeKind kind) {
        this.from = from;
        this.to = to;
        this.field = f;
        this.kind = kind;
    }
}