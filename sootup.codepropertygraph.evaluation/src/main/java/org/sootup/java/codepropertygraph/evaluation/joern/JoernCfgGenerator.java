package org.sootup.java.codepropertygraph.evaluation.joern;

import io.joern.dataflowengineoss.dotgenerator.DdgGenerator;
import io.joern.jimple2cpg.Config;
import io.joern.jimple2cpg.Jimple2Cpg;
import io.shiftleft.codepropertygraph.cpgloading.CpgLoader;
import io.shiftleft.codepropertygraph.cpgloading.CpgLoaderConfig;
import io.shiftleft.codepropertygraph.generated.Cpg;
import io.shiftleft.codepropertygraph.generated.nodes.Call;
import io.shiftleft.codepropertygraph.generated.nodes.Method;
import io.shiftleft.codepropertygraph.generated.nodes.StoredNode;
import io.shiftleft.semanticcpg.dotgenerator.AstGenerator;
import io.shiftleft.semanticcpg.dotgenerator.CdgGenerator;
import io.shiftleft.semanticcpg.dotgenerator.CfgGenerator;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import scala.Option;
import scala.collection.immutable.Seq;
import scala.jdk.CollectionConverters;
import scala.util.Try;

public class JoernCfgGenerator {

  public void generateCFG(String jarFilePath, String outputDirectory) {
    Seq<String> dynamicDirs =
        CollectionConverters.ListHasAsScala(Collections.singletonList(jarFilePath))
            .asScala()
            .toSeq();

    Config config =
        new Config(
            Option.empty(),
            dynamicDirs,
            CollectionConverters.ListHasAsScala(new ArrayList<String>()).asScala().toSeq(),
            false);
    config = (Config) config.withInputPath(jarFilePath);
    config = (Config) config.withOutputPath(outputDirectory);

    Try<Cpg> cpgTry = new Jimple2Cpg().createCpg(config);

    if (cpgTry.isSuccess()) {
      Cpg cpg = cpgTry.get();
      return;
    } else if (cpgTry.isFailure()) {
      Throwable exception = cpgTry.failed().get();
      throw new RuntimeException(exception.getMessage());
    }
  }

  public Cpg readCpg(String cpgPath) {
    overflowdb.Config odbConfig = new overflowdb.Config().withStorageLocation(cpgPath);
    CpgLoaderConfig cpgLoaderConfig = new CpgLoaderConfig(true, odbConfig);
    if (CpgLoader.isLegacyCpg(cpgPath)) {
      System.out.println("legacy");
    }
    return new CpgLoader().loadFromOverflowDb(cpgLoaderConfig);
  }

  public static void main(String[] args) {
    JoernCfgGenerator example = new JoernCfgGenerator();
    example.generateCFG(
        "sootup.codepropertygraph.evaluation/src/main/resources/commons-lang3-3.14.0.jar",
        "sootup.codepropertygraph.evaluation/src/main/temp/someout.cpg");
    if (true) return;
    Cpg cpg = example.readCpg("sootup.codepropertygraph.evaluation/src/main/temp/out.cpg");

    ArrayList<Method> methods = new ArrayList<>();
    cpg.graph().nodes("METHOD").forEachRemaining(node -> methods.add((Method) node));
    System.out.println(methods.get(0).code());

    AstGenerator astGen = new AstGenerator();
    CfgGenerator cfgGen = new CfgGenerator();
    CdgGenerator cdgGen = new CdgGenerator();
    DdgGenerator ddgGen = new DdgGenerator();

    for (Method method : methods) {
      // List<CfgNode> cfgNodes =
      // CollectionConverters.SeqHasAsJava(method.get().cfgFirst().toList()).asJava();

      Graph cfgGraph = cfgGen.generate(method);

      List<StoredNode> vertices =
          CollectionConverters.SeqHasAsJava(cfgGraph.vertices().toSeq()).asJava();
      List<Edge> edges = CollectionConverters.SeqHasAsJava(cfgGraph.edges().toSeq()).asJava();

      System.out.println(((Call) vertices.get(0)).code());
      System.out.println(edges.get(0));

      cfgGraph.edges();
      break;
    }
  }
}
