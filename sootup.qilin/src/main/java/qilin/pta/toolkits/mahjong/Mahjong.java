package qilin.pta.toolkits.mahjong;

import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.pta.toolkits.common.FieldPointstoGraph;
import qilin.util.Stopwatch;

import java.util.Collection;
import java.util.Map;

/**
 * @author Tian Tan
 * @author Yue Li
 */
public class Mahjong {
    public static void run(PTA pta, Map<Object, Object> heapModelMap) {
        FieldPointstoGraph fpg = buildFPG(pta);
        System.out.print("Creating heap abstraction ... ");
        Stopwatch mahjongTimer = Stopwatch.newAndStart("Mahjong");
        HeapAbstraction heapAbs = new HeapAbstraction(fpg);
        Map<AllocNode, AllocNode> mom = heapAbs.computeMergedObjectMap();
        mahjongTimer.stop();
        System.out.println(mahjongTimer);
        outputStatistics(fpg, mom);

        System.out.print("Writing Mahjong heap abstraction ...\n");
        writeMergedObjectMap(mom, heapModelMap);
    }

    public static FieldPointstoGraph buildFPG(PTA pta) {
        System.out.print("Building FPG (Field Points-to Graph) ... ");
        Stopwatch fpgTimer = Stopwatch.newAndStart("FPG Construction");
        FieldPointstoGraph fpg = new FieldPointstoGraph(pta);
        fpgTimer.stop();
        System.out.println(fpgTimer);
        return fpg;
    }

    public static void outputStatistics(FieldPointstoGraph fpg, Map<AllocNode, AllocNode> mom) {
        int nObj = (int) mom.keySet().stream().distinct().count();
        int nObjMahjong = (int) mom.values().stream().distinct().count();
        System.out.println("-----------------------------------------------------------");
        System.out.printf("%d objects in the allocation-site heap abstraction.\n", nObj);
        System.out.printf("%d objects in the Mahjong heap abstraction.\n", nObjMahjong);
        System.out.println("-----------------------------------------------------------");

        int nType = (int) fpg.getAllObjs().stream().map(AllocNode::getType).distinct().count();
        int nField = (int) fpg.getAllObjs().stream().map(fpg::outFieldsOf).flatMap(Collection::stream).distinct().count();
        int nObj2 = fpg.getAllObjs().size();
        // int nObjMahjong = model.size();

        System.out.println("-----------------------------------------------------------");
        System.out.println("In the FPG (allocation-site heap abstraction), there are:");
        System.out.printf("%10d types\n", nType);
        System.out.printf("%10d fields\n", nField);
        System.out.printf("%10d objects\n", nObj2);
        System.out.println("In the Mahjong heap abstraction, there are:");
        System.out.printf("%10d objects\n", nObjMahjong);
        System.out.println("-----------------------------------------------------------");
    }

    private static void writeMergedObjectMap(Map<AllocNode, AllocNode> mom, Map<Object, Object> heapModelMap) {
        mom.forEach((heap, mergedHeap) -> {
            heapModelMap.put(heap.getNewExpr(), mergedHeap.getNewExpr());
        });
    }

}
