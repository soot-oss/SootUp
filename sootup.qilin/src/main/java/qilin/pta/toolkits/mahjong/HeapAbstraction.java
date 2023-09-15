package qilin.pta.toolkits.mahjong;

import qilin.core.pag.AllocNode;
import qilin.core.pag.Node;
import qilin.pta.toolkits.mahjong.automata.DFA;
import qilin.pta.toolkits.mahjong.automata.DFAEquivalenceChecker;
import qilin.pta.toolkits.mahjong.automata.DFAFactory;
import qilin.pta.toolkits.mahjong.automata.DFAState;
import qilin.pta.toolkits.common.FieldPointstoGraph;
import qilin.util.UnionFindSet;
import soot.Type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Tian Tan
 * @author Yue Li
 * refactered by Dongjie He.
 */
public class HeapAbstraction {

    private final FieldPointstoGraph fpg;
    private final DFAFactory dfaFactory;
    private final DFAEquivalenceChecker dfaEqChecker;

    /**
     * This map would be manipulated by multiple threads
     * thus it should be concurrent hashmap.
     */
    private Map<AllocNode, Boolean> canMerged;

    public HeapAbstraction(FieldPointstoGraph fpg) {
        this.fpg = fpg;
        this.dfaFactory = new DFAFactory(fpg);
        this.dfaEqChecker = new DFAEquivalenceChecker();
    }

    public Map<AllocNode, AllocNode> computeMergedObjectMap() {
        UnionFindSet<AllocNode> uf = modelHeap();
        return convertToMap(uf.getDisjointSets());
    }

    /**
     * Modeling the heap by checking the equivalence of automata.
     */
    private UnionFindSet<AllocNode> modelHeap() {
        canMerged = new ConcurrentHashMap<>();
        Set<AllocNode> allObjs = fpg.getAllObjs();
        UnionFindSet<AllocNode> uf = new UnionFindSet<>(allObjs);
        // group the objects by their types
        Map<Type, Set<AllocNode>> groupedObjs = allObjs.stream()
                .collect(Collectors.groupingBy(Node::getType, Collectors.toSet()));
        groupedObjs.entrySet().parallelStream().forEach(entry -> {
            DFAMap dfaMap = new DFAMap();
            Set<AllocNode> objs = entry.getValue().stream().filter(o -> canBeMerged(o, dfaMap)).collect(Collectors.toSet());
            for (AllocNode o1 : objs) {
                for (AllocNode o2 : objs) {
                    if (o1.getNumber() <= o2.getNumber() && !uf.isConnected(o1, o2)) {
                        if (canBeMerged(o1, o2, dfaMap)) {
                            uf.union(o1, o2);
                        }
                    }
                }
            }
        });
        return uf;
    }

    /**
     * @param o1
     * @param o2
     * @return whether o1 and o2 can be merged.
     */
    private boolean canBeMerged(AllocNode o1, AllocNode o2, DFAMap dfaMap) {
        if (o1 == o2) {
            return true;
        }
        DFA dfa1 = dfaMap.retrieveDFA(o1);
        DFA dfa2 = dfaMap.retrieveDFA(o2);
        return dfaEqChecker.isEquivalent(dfa1, dfa2);
    }

    /**
     * @param o
     * @return whether o can be merged with other objects.
     */
    private boolean canBeMerged(AllocNode o, DFAMap dfaMap) {
        if (!canMerged.containsKey(o)) {
            boolean result = true;
            // Check whether the types of objects pointed (directly/indirectly)
            // by o are unique.
            DFA dfa = dfaMap.retrieveDFA(o);
            for (DFAState s : dfa.getStates()) {
                if (dfa.outputOf(s).size() > 1) {
                    // Types pointed (directly/indirectly) by o are not unique.
                    result = false;
                    break;
                }
            }
            canMerged.put(o, result);
        }
        return canMerged.get(o);
    }

    private static Map<AllocNode, AllocNode> convertToMap(Collection<Set<AllocNode>> objModel) {
        Map<AllocNode, AllocNode> map = new HashMap<>();
        objModel.forEach(objs -> {
            AllocNode rep = selectRepresentative(objs);
            objs.forEach(obj -> map.put(obj, rep));
        });
        return map;
    }

    private static AllocNode selectRepresentative(Set<AllocNode> objs) {
        return objs.stream().findFirst().get();
    }

    /**
     * During equivalence check, each thread holds a DFAMap which contains
     * the DFA of the objects of the type. After comparison, the DFAMap
     * and its containing DFA will be released to save memory space.
     */
    private class DFAMap {
        private final Map<AllocNode, DFA> dfaMap = new HashMap<>();

        private DFA retrieveDFA(AllocNode o) {
            return dfaMap.computeIfAbsent(o, k -> dfaFactory.getDFA(o));
        }
    }

}
