package sootup.callgraph;

import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CallGraphFSTCodecTest extends CallGraphAlgorithmTest {

    @Override
    protected RapidTypeAnalysisAlgorithm createAlgorithm(JavaView view) {
        return new RapidTypeAnalysisAlgorithm(view);
    }

    @Test
    public void roundTrip() throws Exception {

//        CallGraph cg =loadCallGraph("RTA", "lic.Class");

        CallGraph cg =loadCallGraph("VirtualCall", "vc4.Class");

//        CallGraph cg = loadCallGraph("CallGraphDifference", "Example");

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CallGraphFSTCodec.write(cg, bos);

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        var rebuilt = CallGraphFSTCodec.read(bis, view);

        // every callee from 'entry' is present after read()
        var origCallees = cg.getCalls().stream().toList();
        var rebuiltCallees = rebuilt.getCalls().stream().toList();
        assertEquals(new HashSet<>(origCallees), new HashSet<>(rebuiltCallees));
    }
}
