package qilin.pta.toolkits.debloaterx;

import qilin.core.pag.PagNode;
import qilin.core.pag.SparkField;

public class Edge {
  PagNode from;
  PagNode to;
  SparkField field;
  EdgeKind kind;

  Edge(PagNode from, PagNode to, SparkField f, EdgeKind kind) {
    this.from = from;
    this.to = to;
    this.field = f;
    this.kind = kind;
  }
}
