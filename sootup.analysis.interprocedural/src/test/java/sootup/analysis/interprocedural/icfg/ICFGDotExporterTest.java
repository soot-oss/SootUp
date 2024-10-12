package sootup.analysis.interprocedural.icfg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.analysis.interprocedural.ifds.IFDSTaintTestSetUp;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.graph.StmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

@Tag("Java8")
public class ICFGDotExporterTest extends IFDSTaintTestSetUp {

  public CallGraph loadCallGraph(JavaView view) {
    CallGraph cg =
        new ClassHierarchyAnalysisAlgorithm(view)
            .initialize(Collections.singletonList(entryMethodSignature));
    assertNotNull(cg);
    assertTrue(
        cg.containsMethod(entryMethodSignature),
        entryMethodSignature + " is not found in CallGraph");
    return cg;
  }

  @Test
  public void ICFGDotExportTest() {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(new JavaClassPathAnalysisInputLocation("src/test/resources/icfg/binary"));

    view = new JavaView(inputLocations);

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaClassType mainClassSignature = identifierFactory.getClassType("ICFGExample");

    SootClass sc = view.getClass(mainClassSignature).get();
    entryMethod =
        sc.getMethods().stream().filter(e -> e.getName().equals("entryPoint")).findFirst().get();

    entryMethodSignature = entryMethod.getSignature();

    JimpleBasedInterproceduralCFG icfg =
        new JimpleBasedInterproceduralCFG(
            view, Collections.singletonList(entryMethodSignature), false, false);
    CallGraph callGraph = loadCallGraph(view);
    String expectedCallGraph = icfg.buildICFGGraph(callGraph);
    Digraph digraph = parseDigraph(expectedCallGraph);
    assertEquals(digraph.blocks.length, 5);
    // As per the example code, the first block has no invoke calls, so the number of statements and
    // edges should be same
    assertEquals(digraph.blocks[0].statements.length, digraph.blocks[0].edges.size());
    // As per the example code, the second block has an invoke call, so the number of edges should
    // be same one more than the statements, as one edge is for the invoke call
    assertEquals(digraph.blocks[2].statements.length + 1, digraph.blocks[2].edges.size());
    // compute the edges from the callGraph and compare the edges with the ICFGCallGraph created
    assertEquals(
        edgesFromCallGraph(entryMethodSignature, icfg, callGraph),
        String.join(" -> ", digraph.blocks[0].edges));
  }

  @Test
  public void ICFGDotExportTest2() {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(new JavaClassPathAnalysisInputLocation("src/test/resources/icfg/binary"));

    view = new JavaView(inputLocations);

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaClassType mainClassSignature = identifierFactory.getClassType("ICFGExample2");

    SootClass sc = view.getClass(mainClassSignature).get();
    entryMethod =
        sc.getMethods().stream().filter(e -> e.getName().equals("entryPoint")).findFirst().get();

    entryMethodSignature = entryMethod.getSignature();

    JimpleBasedInterproceduralCFG icfg =
        new JimpleBasedInterproceduralCFG(
            view, Collections.singletonList(entryMethodSignature), false, false);
    CallGraph callGraph = loadCallGraph(view);
    String expectedCallGraph = icfg.buildICFGGraph(callGraph);
    Digraph digraph = parseDigraph(expectedCallGraph);
    assertEquals(digraph.blocks.length, 7);
    // As per the example code, the first block has no invoke calls, so the number of statements and
    // edges should be same
    assertEquals(digraph.blocks[0].statements.length, digraph.blocks[0].edges.size());
    // As per the example code, the second block has an invoke call, so the number of edges should
    // be same one more than the statements, as one edge is for the invoke call
    assertEquals(digraph.blocks[2].statements.length + 1, digraph.blocks[2].edges.size());
    // compute the edges from the callGraph and compare the edges with the ICFGCallGraph created
    assertEquals(
        edgesFromCallGraph(entryMethodSignature, icfg, callGraph),
        String.join(" -> ", digraph.blocks[0].edges));
  }

  @Test
  public void ICFGArrayListDotExport() {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(new JavaClassPathAnalysisInputLocation("src/test/resources/icfg/binary"));

    view = new JavaView(inputLocations);

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaClassType mainClassSignature = identifierFactory.getClassType("ICFGArrayListExample");

    SootClass sc = view.getClass(mainClassSignature).get();
    entryMethod =
        sc.getMethods().stream().filter(e -> e.getName().equals("main")).findFirst().get();

    entryMethodSignature = entryMethod.getSignature();

    JimpleBasedInterproceduralCFG icfg =
        new JimpleBasedInterproceduralCFG(
            view, Collections.singletonList(entryMethodSignature), false, false);
    CallGraph callGraph = loadCallGraph(view);
    String expectedCallGraph = icfg.buildICFGGraph(callGraph);
    Digraph digraph = parseDigraph(expectedCallGraph);
    assertEquals(
        edgesFromCallGraph(entryMethodSignature, icfg, callGraph),
        String.join(" -> ", digraph.blocks[0].edges));
  }

  @Test
  public void ICFGInterfaceDotExport() {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(new JavaClassPathAnalysisInputLocation("src/test/resources/icfg/binary"));

    view = new JavaView(inputLocations);

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaClassType mainClassSignature = identifierFactory.getClassType("ICFGInterfaceExample");

    SootClass sc = view.getClass(mainClassSignature).get();
    entryMethod =
        sc.getMethods().stream().filter(e -> e.getName().equals("main")).findFirst().get();

    entryMethodSignature = entryMethod.getSignature();

    JimpleBasedInterproceduralCFG icfg =
        new JimpleBasedInterproceduralCFG(
            view, Collections.singletonList(entryMethodSignature), false, false);
    CallGraph callGraph = loadCallGraph(view);
    String expectedCallGraph = icfg.buildICFGGraph(callGraph);
    Digraph digraph = parseDigraph(expectedCallGraph);
    assertEquals(
        edgesFromCallGraph(entryMethodSignature, icfg, callGraph),
        String.join(" -> ", digraph.blocks[0].edges));
  }

  /** Compute the Edges of the given methodSignature from the provided callGraph */
  public String edgesFromCallGraph(
      MethodSignature methodSignature, JimpleBasedInterproceduralCFG icfg, CallGraph callGraph) {
    Map<MethodSignature, StmtGraph<?>> signatureToStmtGraph = new LinkedHashMap<>();
    icfg.computeAllCalls(
        Collections.singletonList(methodSignature), signatureToStmtGraph, callGraph);
    Map<Integer, MethodSignature> calls;
    calls = ICFGDotExporter.computeCalls(signatureToStmtGraph, view, callGraph);
    final Optional<? extends SootMethod> methodOpt = view.getMethod(methodSignature);
    if (methodOpt.isPresent()) {
      SootMethod sootMethod = methodOpt.get();
      if (sootMethod.hasBody()) {
        String edges = connectEdges(sootMethod.getBody().getStmts(), methodSignature, calls);
        return edges;
      }
    }
    return "";
  }

  /**
   * Compute the possible edges which includes the hashcodes of each flowing statement from the
   * given statements
   *
   * @param stmts
   * @param methodSignature
   * @param calls
   * @return
   */
  public String connectEdges(
      List<Stmt> stmts, MethodSignature methodSignature, Map<Integer, MethodSignature> calls) {
    StringBuilder sb = new StringBuilder();
    boolean isAdded = false;
    for (Stmt stmt : stmts) {
      if (methodSignature != null && calls != null) {
        for (Map.Entry<Integer, MethodSignature> entry : calls.entrySet()) {
          int key = entry.getKey();
          MethodSignature value = entry.getValue();
          if (methodSignature.equals(value) && !isAdded) {
            sb.append(key).append(" -> ");
            isAdded = true;
          }
        }
      }
      sb.append(stmt.hashCode()).append(" -> ");
    }
    sb.delete(sb.length() - 4, sb.length());
    return sb.toString();
  }

  /** A POJO class to convert the dot-exported string into java object */
  class Block {
    String label;
    String[] statements;

    List<String> edges;

    public Block() {
      edges = new ArrayList<>();
    }
  }

  class Digraph {
    Block[] blocks;
  }

  public Digraph parseDigraph(String digraphString) {
    Digraph digraph = new Digraph();
    Block currentBlock = null;

    String[] lines = digraphString.split("\n");
    for (String line : lines) {
      line = line.trim();
      if (line.startsWith("subgraph cluster_")) {
        String label = line.split("subgraph cluster_")[1].trim();
        currentBlock = new Block();
        currentBlock.label = label;
        digraph.blocks = addBlock(digraph.blocks, currentBlock);
      } else if (currentBlock != null && line.contains("label=")) {
        String[] splitArray = line.split("=");
        String[] newArray = new String[splitArray.length - 1];
        System.arraycopy(splitArray, 1, newArray, 0, splitArray.length - 1);
        String join = String.join("", newArray);
        String statement = line.split("=")[1].replace("\"", "").trim();
        currentBlock.statements = addStatement(currentBlock.statements, join);
      } else if (line.contains("->")) {
        String[] arrows = line.split("->");
        for (String arrow : arrows) {
          String trimmedArrow = arrow.trim();
          if (!trimmedArrow.isEmpty()) {
            currentBlock.edges.add(trimmedArrow);
          }
        }
      }
    }

    return digraph;
  }

  public static Block[] addBlock(Block[] blocks, Block block) {
    if (blocks == null) {
      blocks = new Block[1];
      blocks[0] = block;
    } else {
      Block[] temp = new Block[blocks.length + 1];
      System.arraycopy(blocks, 0, temp, 0, blocks.length);
      temp[blocks.length] = block;
      blocks = temp;
    }
    return blocks;
  }

  public static String[] addStatement(String[] statements, String statement) {
    if (statements == null) {
      statements = new String[1];
      statements[0] = statement;
    } else {
      String[] temp = new String[statements.length + 1];
      System.arraycopy(statements, 0, temp, 0, statements.length);
      temp[statements.length] = statement;
      statements = temp;
    }
    return statements;
  }
}
