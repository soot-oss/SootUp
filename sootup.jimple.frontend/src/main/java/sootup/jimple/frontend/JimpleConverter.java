package sootup.jimple.frontend;

/*-
 * #%L
 * SootUp\
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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

import java.nio.file.Path;
import java.util.List;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.ResolveException;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.JimpleUtils;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.*;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.SootClassMemberSignature;
import sootup.core.signatures.SootClassMemberSubSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;
import sootup.java.core.OverridingJavaClassSource;
import sootup.jimple.JimpleParser;

public class JimpleConverter {

  private boolean useLazyResolution = false;

  public JimpleConverter() {}

  public JimpleConverter(boolean useLazyResolution) {
    this.useLazyResolution = useLazyResolution;
  }

  public OverridingJavaClassSource run(
      @NonNull CharStream charStream,
      @NonNull AnalysisInputLocation inputlocation,
      @NonNull Path sourcePath,
      @NonNull List<BodyInterceptor> bodyInterceptors,
      @NonNull View view) {

    final JimpleParser jimpleParser =
        JimpleConverterUtil.createJimpleParser(charStream, sourcePath);
    jimpleParser.setErrorHandler(new BailErrorStrategy());

    return run(jimpleParser, inputlocation, sourcePath, bodyInterceptors, view);
  }

  public OverridingJavaClassSource run(
      @NonNull JimpleParser parser,
      @NonNull AnalysisInputLocation inputlocation,
      @NonNull Path sourcePath,
      @NonNull List<BodyInterceptor> bodyInterceptors,
      @NonNull View view) {

    ClassVisitor classVisitor;
    try {
      classVisitor = new ClassVisitor(sourcePath, bodyInterceptors, view, useLazyResolution);
      classVisitor.visit(parser.file());
    } catch (ParseCancellationException ex) {
      throw new ResolveException("Syntax Error", sourcePath, ex);
    }

    return new OverridingJavaClassSource(
        classVisitor.methods,
        classVisitor.fields,
        classVisitor.modifiers,
        classVisitor.interfaces,
        classVisitor.superclass,
        classVisitor.outerclass,
        classVisitor.position,
        sourcePath,
        classVisitor.clazz,
        inputlocation);
  }
}
