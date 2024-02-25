package org.sootup.java.codepropertygraph.evaluation.graph.processing;

import io.joern.dataflowengineoss.DefaultSemantics;
import io.joern.dataflowengineoss.dotgenerator.DdgGenerator;
import io.shiftleft.codepropertygraph.cpgloading.CpgLoader;
import io.shiftleft.codepropertygraph.cpgloading.CpgLoaderConfig;
import io.shiftleft.codepropertygraph.generated.Cpg;
import io.shiftleft.codepropertygraph.generated.nodes.Method;
import io.shiftleft.semanticcpg.dotgenerator.CdgGenerator;
import io.shiftleft.semanticcpg.dotgenerator.CfgGenerator;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.*;
import java.util.*;

public class JoernProcessor {
  private final Map<String, Method> methods = new HashMap<>();

  public JoernProcessor(String cpgPath) {
    overflowdb.Config odbConfig = new overflowdb.Config().withStorageLocation(cpgPath);
    CpgLoaderConfig cpgLoaderConfig = new CpgLoaderConfig(true, odbConfig);
    Cpg cpg = new CpgLoader().loadFromOverflowDb(cpgLoaderConfig);

    cpg.graph()
        .nodes("METHOD")
        .forEachRemaining(
            node -> {
              Method method = (Method) node;
              String methodName = method.fullName().replace("'", "");
              methods.put(methodName, method);
            });
  }

  public Graph generateCfg(Method method) {
    return new CfgGenerator().generate(method);
  }

  public Graph generateCdg(Method method) {
    return new CdgGenerator().generate(method);
  }

  public Graph generateDdg(Method method) {
    return new DdgGenerator().generate(method, DefaultSemantics.apply());
  }

  public Optional<Method> getMethod(String methodSignature) {
    return Optional.ofNullable(methods.getOrDefault(methodSignature, null));
  }
}
