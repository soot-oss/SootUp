package org.sootup.java.codepropertygraph.evaluation.sootup;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.cfg.CfgCreator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphEdge;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphNode;
import sootup.java.core.views.JavaView;

public class SootUpCfgGenerator {
  private final Map<String, SootMethod> methods = new HashMap<>();

  public SootUpCfgGenerator(String sourceCodeDirPath) {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new JavaClassPathAnalysisInputLocation(sourceCodeDirPath));
    View view = new JavaView(inputLocations);

    view.getClasses()
        .forEach(cl -> cl.getMethods().forEach(m -> methods.put(getMethodSignatureAsJoern(m), m)));
  }

  public List<SootMethod> getMethods() {
    return new ArrayList<>(methods.values());
  }

  public PropertyGraph getCfg(SootMethod method) {
    return CfgCreator.convert(new MethodInfo(method));
  }

  public List<PropertyGraphEdge> getGraphEdges(PropertyGraph graph) {
    return graph.getEdges();
  }

  public List<PropertyGraphNode> getGraphVertices(PropertyGraph graph) {
    return graph.getNodes();
  }

  public String getMethodSignatureAsJoern(SootMethod method) {
    ClassType declaringClassType = method.getDeclaringClassType();
    String className = declaringClassType.getFullyQualifiedName();
    String methodName = method.getName();
    String methodReturnType = method.getReturnType().toString();
    String methodParams =
        String.format(
            "(%s)",
            method.getParameterTypes().stream()
                .map(Object::toString)
                .collect(Collectors.joining(",")));

    return String.format("%s.%s:%s%s", className, methodName, methodReturnType, methodParams);
  }
}
