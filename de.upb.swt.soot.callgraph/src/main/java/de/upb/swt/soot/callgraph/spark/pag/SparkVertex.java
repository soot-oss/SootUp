package de.upb.swt.soot.callgraph.spark.pag;

import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;

import javax.annotation.Nonnull;

public class SparkVertex {
    @Nonnull
    final Node node;

    public SparkVertex(@Nonnull Node node) {
        this.node = node;
    }
}
