package org.sootup.java.codepropertygraph.evaluation.joern;

import io.shiftleft.codepropertygraph.cpgloading.CpgLoader;
import io.shiftleft.codepropertygraph.cpgloading.CpgLoaderConfig;
import io.shiftleft.codepropertygraph.generated.Cpg;
import io.shiftleft.codepropertygraph.generated.nodes.Method;
import io.shiftleft.codepropertygraph.generated.nodes.StoredNode;
import io.shiftleft.semanticcpg.dotgenerator.AstGenerator;
import io.shiftleft.semanticcpg.dotgenerator.CfgGenerator;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.*;
import java.util.*;
import scala.jdk.CollectionConverters;

public class JoernCfgGenerator {
  private final Cpg cpg;
  private final Map<String, Method> methods = new HashMap<>();

  public JoernCfgGenerator(String cpgPath) {
    overflowdb.Config odbConfig = new overflowdb.Config().withStorageLocation(cpgPath);
    CpgLoaderConfig cpgLoaderConfig = new CpgLoaderConfig(true, odbConfig);
    this.cpg = new CpgLoader().loadFromOverflowDb(cpgLoaderConfig);

    cpg.graph()
        .nodes("METHOD")
        .forEachRemaining(
            node -> {
              Method method = (Method) node;
              String methodName = method.fullName().replace("'", "");
              methods.put(methodName, method);
            });
  }

  public Cpg getCpg() {
    return cpg;
  }

  public List<Method> getMethods() {
    return new ArrayList<>(methods.values());
  }

  public Graph getAst(Method method) {
    return new AstGenerator().generate(method);
  }

  public Graph getCfg(Method method) {
    return new CfgGenerator().generate(method);
  }

  public List<Edge> getGraphEdges(Graph graph) {
    return CollectionConverters.SeqHasAsJava(graph.edges().toSeq()).asJava();
  }

  public List<StoredNode> getGraphVertices(Graph graph) {
    return CollectionConverters.SeqHasAsJava(graph.vertices().toSeq()).asJava();
  }

  public Optional<Method> getMethod(String methodSignature) {
    return Optional.ofNullable(methods.getOrDefault(methodSignature, null));
  }
}
