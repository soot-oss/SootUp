package de.upb.swt.soot.callgraph.spark.pag;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import de.upb.swt.soot.callgraph.spark.builder.GlobalNodeFactory;
import de.upb.swt.soot.callgraph.spark.builder.MethodNodeFactory;
import de.upb.swt.soot.callgraph.spark.builder.NodeConstants;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class MiscEdgeHandler {

  private MiscEdgeHandler() {}

  private static final JavaIdentifierFactory identifierFactory =
      JavaIdentifierFactory.getInstance();
  private static final JavaClassType strType = identifierFactory.getClassType(NodeConstants.STRING);
  private static final ArrayType strArrayType = identifierFactory.getArrayType(strType, 1);
  private static final String INIT = "<init>";
  private static final String VOID = "void";
  private static final MethodSubSignature mainSubSignature =
      identifierFactory.getMethodSubSignature(
          "main", VoidType.getInstance(), Collections.singleton(strArrayType));
  private static final MethodSignature threadInitSignature =
      identifierFactory.getMethodSignature(
          INIT,
          NodeConstants.THREAD,
          VOID,
          Arrays.asList(NodeConstants.THREAD_GROUP, NodeConstants.STRING));
  private static final MethodSignature finalizerInitSignature =
      identifierFactory.getMethodSignature(
          INIT, NodeConstants.FINALIZER, VOID, Arrays.asList(NodeConstants.OBJECT));
  private static final MethodSignature finalizerRunFinalizerSignature =
      identifierFactory.getMethodSignature(
          "runFinalizer", NodeConstants.FINALIZER, VOID, Collections.emptyList());
  private static final MethodSignature finalizerAccess100Signature =
      identifierFactory.getMethodSignature(
          "access$100", NodeConstants.FINALIZER, VOID, Arrays.asList(NodeConstants.OBJECT));
  private static final MethodSignature classLoaderInitSignature =
      identifierFactory.getMethodSignature(
          INIT, NodeConstants.CLASS_LOADER, VOID, Collections.emptyList());
  private static final MethodSignature threadExitSignature =
      identifierFactory.getMethodSignature(
          "exit", NodeConstants.THREAD, VOID, Collections.emptyList());
  private static final MethodSignature privilegedActionExInitSignature =
      identifierFactory.getMethodSignature(
          INIT,
          NodeConstants.PRIVILEGED_ACTION_EXCEPTION,
          VOID,
          Arrays.asList(NodeConstants.EXCEPTION));

  public static List<Pair<Node, Node>> getMiscEdge(
      SootMethod method, GlobalNodeFactory globalNodeFactory, MethodNodeFactory methodNodeFactory) {
    MethodSignature signature = method.getSignature();
    if (method.getSignature().getSubSignature().equals(mainSubSignature)) {
      Pair<Node, Node> argToParam =
          new ImmutablePair<>(globalNodeFactory.caseArgv(), methodNodeFactory.caseParameter(0));
      return Arrays.asList(argToParam);
    } else if (signature.equals(threadInitSignature)) {
      Pair<Node, Node> threadToThis =
          new ImmutablePair<>(globalNodeFactory.caseMainThread(), methodNodeFactory.caseThis());
      Pair<Node, Node> threadGroupToParam =
          new ImmutablePair<>(
              globalNodeFactory.caseMainThreadGroup(), methodNodeFactory.caseParameter(0));
      return Arrays.asList(threadToThis, threadGroupToParam);
    } else if (signature.equals(finalizerInitSignature)) {
      Pair<Node, Node> thisToFinalize =
          new ImmutablePair<>(methodNodeFactory.caseThis(), globalNodeFactory.caseFinalizeQueue());
      return Arrays.asList(thisToFinalize);
    } else if (signature.equals(finalizerRunFinalizerSignature)) {
      Pair<Node, Node> finalizeToThis =
          new ImmutablePair<>(globalNodeFactory.caseFinalizeQueue(), methodNodeFactory.caseThis());
      return Arrays.asList(finalizeToThis);
    } else if (signature.equals(finalizerAccess100Signature)) {
      Pair<Node, Node> finalizeToParam =
          new ImmutablePair<>(
              globalNodeFactory.caseFinalizeQueue(), methodNodeFactory.caseParameter(0));
      return Arrays.asList(finalizeToParam);
    } else if (signature.equals(classLoaderInitSignature)) {
      Pair<Node, Node> defaultCLToThis =
          new ImmutablePair<>(
              globalNodeFactory.caseDefaultClassLoader(), methodNodeFactory.caseThis());
      return Arrays.asList(defaultCLToThis);
    } else if (signature.equals(threadExitSignature)) {
      Pair<Node, Node> threadToThis =
          new ImmutablePair<>(globalNodeFactory.caseMainThread(), methodNodeFactory.caseThis());
      return Arrays.asList(threadToThis);
    } else if (signature.equals(privilegedActionExInitSignature)) {
      Pair<Node, Node> throwToParam =
          new ImmutablePair<>(globalNodeFactory.caseThrow(), methodNodeFactory.caseParameter(0));
      Pair<Node, Node> privilegedActionExToThis =
          new ImmutablePair<>(
              globalNodeFactory.casePrivilegedActionException(), methodNodeFactory.caseThis());
      return Arrays.asList(throwToParam, privilegedActionExToThis);
    }
    return Collections.emptyList();

    // Following TODOs depend on method.getNumberedSubSignature
    // TODO: method.getNumberedSubSignature().equals(sigCanonicalize)

    // TODO: isImplicit
  }
}
