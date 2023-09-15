package qilin.pta.toolkits.common;

import qilin.core.PTA;
import qilin.core.pag.AllocNode;

import java.util.HashSet;
import java.util.Set;

public class DebloatedOAG extends OAG {
    private final Set<AllocNode> ctxDepHeaps;

    public DebloatedOAG(PTA prePta, Set<AllocNode> ctxDepHeaps) {
        super(prePta);
        this.ctxDepHeaps = ctxDepHeaps;
    }

    @Override
    /**
     * Add a directed object allocation edge to the OAG.
     */
    protected void addEdge(AllocNode src, AllocNode tgt) {
        nodes.add(src);
        nodes.add(tgt);
        if (this.ctxDepHeaps.contains(tgt)) {
            this.predecessors.computeIfAbsent(tgt, k -> new HashSet<>()).add(src);
            this.successors.computeIfAbsent(src, k -> new HashSet<>()).add(tgt);
        }
    }
}
