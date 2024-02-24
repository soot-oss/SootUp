package org.sootup.java.codepropertygraph.evaluation.graph.processing;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.sootup.java.codepropertygraph.evaluation.normalizers.DynamicInvokeNormalizer;
import org.sootup.java.codepropertygraph.evaluation.normalizers.HashSuffixEliminator;
import org.sootup.java.codepropertygraph.evaluation.normalizers.InterfaceInvokeNormalizer;
import org.sootup.java.codepropertygraph.evaluation.normalizers.SpecialInvokeNormalizer;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.jimple.parser.JimpleAnalysisInputLocation;
import sootup.jimple.parser.JimpleView;

public class SootUpProcessor {
  private final Map<String, SootMethod> methods = new HashMap<>();

  public SootUpProcessor(Path sourceCodeDirPath) {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(
        new JimpleAnalysisInputLocation(
            sourceCodeDirPath,
            null,
            Arrays.asList(
                // new UnreachableCodeEliminator(),
                // new LocalSplitter(),
                new HashSuffixEliminator(),
                new DynamicInvokeNormalizer(),
                new SpecialInvokeNormalizer(),
                new InterfaceInvokeNormalizer())));
    JimpleView view = new JimpleView(inputLocations);

    view.getClasses()
        .forEach(cl -> cl.getMethods().forEach(m -> methods.put(getMethodSignatureAsJoern(m), m)));
  }

  public List<SootMethod> getMethods() {
    return new ArrayList<>(methods.values());
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

    String methodSignatureAsJoern =
        String.format("%s.%s:%s%s", className, methodName, methodReturnType, methodParams);
    methodSignatureAsJoern = methodSignatureAsJoern.replace("'", "");

    return methodSignatureAsJoern;
  }
}
