package org.sootup.java.codepropertygraph.evaluation.graph.generation;

import io.shiftleft.codepropertygraph.generated.nodes.Method;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer;
import org.sootup.java.codepropertygraph.evaluation.graph.processing.JoernProcessor;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.cdg.CdgCreator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;

public class CdgGraphGenerator extends AbstractGraphGenerator {
  @Override
  PropertyGraph generateSootUpGraph(SootMethod sootUpMethod) {
    return CdgCreator.convert(new MethodInfo(sootUpMethod));
  }

  @Override
  DotSerializer.Graph generateJoernGraph(JoernProcessor joernProcessor, Method joernMethod) {
    return joernProcessor.generateCdg(joernMethod);
  }
}
