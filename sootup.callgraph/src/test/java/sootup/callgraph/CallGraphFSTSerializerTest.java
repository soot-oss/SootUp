package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

/**
 * Unit tests for {@link CallGraphFSTSerializer}.
 *
 * <p>This test verifies that a call graph can be serialized to file and then deserialized back to
 * an equivalent call-graph object. It also checks that invalid input is handled by throwing an
 * exception.
 */
public class CallGraphFSTSerializerTest extends CallGraphAlgorithmTest {

  @Override
  protected ClassHierarchyAnalysisAlgorithm createAlgorithm(JavaView view) {
    return new ClassHierarchyAnalysisAlgorithm(view);
  }

  @Test
  public void testSerializingCallGraph() throws Exception {

    //    This call graph contains several calls (including multiple calls to the same target)
    CallGraph cg = loadCallGraph("Misc", "multi.MultipleCallsToSameTarget");

    Path path = Path.of("src/test/resources/callgraph/Serializer/callGraphSeralizer.txt");

    // Serialize the call graph to the file at path.
    CallGraphFSTSerializer.write(cg, path);

    // Deserialize from the file into a fresh call graph instance.
    var rebuilt = CallGraphFSTSerializer.read(path, view);

    // Convert both call lists to sets and compare to avoid ordering issues.
    var origCallees = cg.getCalls().stream().toList();
    var rebuiltCallees = rebuilt.getCalls().stream().toList();

    assertEquals(new HashSet<>(origCallees), new HashSet<>(rebuiltCallees));

    //    Negative test: ensure passing a null call graph to the serializer results in an exception.
    CallGraph nullCg = null;
    assertThrows(RuntimeException.class, () -> CallGraphFSTSerializer.write(nullCg, path));
  }
}
