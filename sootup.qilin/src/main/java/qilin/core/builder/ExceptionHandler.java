/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.core.builder;

import java.util.*;
import qilin.core.PTA;
import qilin.core.context.Context;
import qilin.core.pag.*;
import qilin.util.JavaTypes;
import qilin.util.sets.P2SetVisitor;
import qilin.util.sets.PointsToSetInternal;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

public class ExceptionHandler {
  protected final Map<PagNode, Collection<ExceptionThrowSite>> throwNodeToSites;
  protected PTA pta;
  protected PAG pag;

  public ExceptionHandler(PTA pta) {
    this.pta = pta;
    this.pag = pta.getPag();
    this.throwNodeToSites = new HashMap<>((int) pta.getView().getClasses().count());
  }

  public Collection<ExceptionThrowSite> throwSitesLookUp(VarNode throwNode) {
    return throwNodeToSites.getOrDefault(throwNode, Collections.emptySet());
  }

  public boolean addThrowSite(PagNode throwNode, ExceptionThrowSite ets) {
    Collection<ExceptionThrowSite> throwSites =
        throwNodeToSites.computeIfAbsent(throwNode, k -> new HashSet<>());
    return throwSites.add(ets);
  }

  public void exceptionDispatch(PointsToSetInternal p2set, ExceptionThrowSite site) {
    p2set.forall(
        new P2SetVisitor(pta) {
          public void visit(PagNode n) {
            dispatch((AllocNode) n, site);
          }
        });
  }

  /*
   * dispatch the exception objects by following the exception-cath-links in Doop-ISSTA09.
   * */
  public void dispatch(AllocNode throwObj, ExceptionThrowSite site) {
    Type type = throwObj.getType();
    ContextMethod momc = site.container();
    SootMethod sm = momc.method();
    Context context = momc.context();
    MethodPAG mpag = pag.getMethodPAG(sm);
    MethodNodeFactory nodeFactory = mpag.nodeFactory();
    VarNode throwNode = site.getThrowNode();
    Stmt handler =
        selectHandler(
            type, mpag.stmt2Handlers.getOrDefault(site.getUnit(), Collections.emptyMap()));
    if (handler != null) {
      assert handler instanceof JIdentityStmt;
      JIdentityStmt handlerStmt = (JIdentityStmt) handler;
      PagNode caughtParam = nodeFactory.getNode(handlerStmt.getRightOp());
      PagNode dst = pta.parameterize(caughtParam, context);
      pag.addEdge(throwObj, dst);
      // record an edge from base --> caughtParam on the methodPag.
      recordImplictEdge(throwNode, caughtParam, mpag);
      return;
    }
    // No trap handle the throwable object in the method.
    PagNode methodThrowNode = nodeFactory.caseMethodThrow();
    PagNode dst = pta.parameterize(methodThrowNode, context);
    pag.addEdge(throwObj, dst);
    // record an edge from base --> methodThrowNode on the methodPag.
    recordImplictEdge(throwNode, methodThrowNode, mpag);
  }

  /**
   * Picks the handler that catches a {@code thrownType} object among the ones in scope at a throw
   * site. SootUp's block graph keeps the handlers of a statement in an unordered, type-keyed map
   * (see {@code MethodPAG.buildException()}), so the bytecode's first-match-wins order is
   * re-derived here by taking the most specific matching caught type - within one {@code try} that
   * is the same choice, since Java rejects a {@code catch} of a subtype after its supertype. Every
   * candidate is a supertype of the thrown class type, so they form a chain and the minimum is
   * well defined; the type-name comparison is only a defensive tie-break that keeps the result
   * independent of map iteration order should two incomparable types ever both match.
   *
   * @return the handler's first statement, or null if nothing here catches the object.
   */
  private Stmt selectHandler(Type thrownType, Map<ClassType, Stmt> handlers) {
    ClassType bestType = null;
    Stmt best = null;
    for (Map.Entry<ClassType, Stmt> handler : handlers.entrySet()) {
      ClassType caughtType = handler.getKey();
      if (!JavaTypes.canStoreType(pta.getView(), thrownType, caughtType)) {
        continue;
      }
      if (bestType == null
          || JavaTypes.canStoreType(pta.getView(), caughtType, bestType)
          || (!JavaTypes.canStoreType(pta.getView(), bestType, caughtType)
              && caughtType.getFullyQualifiedName().compareTo(bestType.getFullyQualifiedName())
                  < 0)) {
        bestType = caughtType;
        best = handler.getValue();
      }
    }
    return best;
  }

  private void recordImplictEdge(PagNode src, PagNode dst, MethodPAG mpag) {
    if (src instanceof ContextVarNode) {
      src = ((ContextVarNode) src).base();
    }
    mpag.addExceptionEdge(src, dst);
  }
}
