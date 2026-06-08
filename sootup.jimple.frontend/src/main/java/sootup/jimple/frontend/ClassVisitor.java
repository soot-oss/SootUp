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
import java.util.*;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.frontend.ResolveException;
import sootup.core.jimple.JimpleUtils;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.Body;
import sootup.core.model.ClassModifier;
import sootup.core.model.FieldModifier;
import sootup.core.model.MethodModifier;
import sootup.core.model.Position;
import sootup.core.signatures.MethodSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.*;
import sootup.core.views.View;
import sootup.java.core.JavaSootField;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.jimple.JimpleBaseVisitor;
import sootup.jimple.JimpleParser;

public class ClassVisitor extends JimpleBaseVisitor<Boolean> {

  @NonNull final IdentifierFactory identifierFactory;
  @NonNull final JimpleConverterUtil util;
  @NonNull final Path path;
  @NonNull final List<BodyInterceptor> bodyInterceptors;
  @NonNull final View view;
  final boolean useLazyResolution;

  ClassType clazz = null;
  Set<JavaSootField> fields = new HashSet<>();
  Set<JavaSootMethod> methods = new HashSet<>();
  JavaClassType superclass = null;
  Set<JavaClassType> interfaces = null;
  JavaClassType outerclass = null; // currently not determined in Java etc -> heuristic will be used
  Position position = NoPositionInformation.getInstance();
  EnumSet<ClassModifier> modifiers = null;

  public ClassVisitor(
      @NonNull Path path,
      @NonNull List<BodyInterceptor> bodyInterceptors,
      @NonNull View view,
      boolean useLazyResolution) {
    this.path = path;
    util = new JimpleConverterUtil(path);
    this.bodyInterceptors = bodyInterceptors;
    this.view = view;
    this.identifierFactory = view.getIdentifierFactory();
    this.useLazyResolution = useLazyResolution;
  }

  @Override
  @NonNull
  public Boolean visitFile(JimpleParser.@NonNull FileContext ctx) {

    position = JimpleConverterUtil.buildPositionFromCtx(ctx);

    // imports
    ctx.importItem().stream().filter(item -> item.location != null).forEach(util::addImport);

    // class_name
    if (ctx.classname != null) {

      // "$" in classname is a heuristic for an inner/outer class
      final String classname = ctx.classname.getText();
      final int dollarPostition = classname.indexOf('$');
      if (dollarPostition > -1) {
        outerclass = (JavaClassType) util.getClassType(classname.substring(0, dollarPostition));
      }
      clazz = util.getClassType(classname);

    } else {
      throw new ResolveException(
          "Classname is not well formed.", path, JimpleConverterUtil.buildPositionFromCtx(ctx));
    }

    modifiers = getClassModifiers(ctx.class_modifier());
    // file_type
    if (ctx.file_type() != null) {
      if (ctx.file_type().getText().equals("interface")) {
        modifiers.add(ClassModifier.INTERFACE);
      }
      if (ctx.file_type().getText().equals("annotation")) {
        modifiers.add(ClassModifier.ANNOTATION);
      }
    }

    // extends_clause
    if (ctx.extends_clause() != null) {
      superclass = (JavaClassType) util.getClassType(ctx.extends_clause().classname.getText());
    } else {
      superclass = null;
    }

    // implements_clause
    if (ctx.implements_clause() != null) {
      interfaces = util.getClassTypeSet(ctx.implements_clause().type_list());
    } else {
      interfaces = Collections.emptySet();
    }

    // member
    for (int i = 0; i < ctx.member().size(); i++) {
      if (ctx.member(i).method() != null) {
        final JavaSootMethod m =
            (JavaSootMethod) new MethodVisitor(this).visitMember(ctx.member(i));
        if (methods.stream()
            .anyMatch(
                meth -> {
                  final MethodSignature signature = m.getSignature();
                  return meth.getSignature().equals(signature);
                })) {
          throw new ResolveException(
              "Method with the same Signature does already exist.", path, m.getPosition());
        }
        if (useLazyResolution) {
          // For lazy resolution, just add the method as-is without eagerly resolving the body
          methods.add(m);
        } else {
          // Eager resolution: apply body interceptors immediately
          if (m.isConcrete()) {
            Body.BodyBuilder bodyBuilder = Body.builder(m.getBody(), m.getModifiers());
            for (BodyInterceptor bodyInterceptor : bodyInterceptors) {
              try {
                bodyInterceptor.interceptBody(bodyBuilder, view);
                bodyBuilder
                    .getControlFlowGraph()
                    .validateStmtConnectionsInGraph(); // TODO: remove in the future ;-)
              } catch (Exception e) {
                throw new IllegalStateException(
                    "Failed to apply " + bodyInterceptor + " to " + m.getSignature(), e);
              }
            }
            Body modifiedBody = bodyBuilder.build();
            JavaSootMethod sm =
                new JavaSootMethod(
                    new OverridingBodySource(m.getBodySource()).withBody(modifiedBody),
                    m.getSignature(),
                    m.getModifiers(),
                    m.getExceptionSignatures(),
                    m.getPosition());
            methods.add(sm);
          } else {
            methods.add(m);
          }
        }
      } else {
        final JimpleParser.FieldContext fieldCtx = ctx.member(i).field();
        EnumSet<FieldModifier> modifier = getFieldModifiers(fieldCtx.field_modifier());
        final Position pos = JimpleConverterUtil.buildPositionFromCtx(fieldCtx);
        final String fieldName = JimpleUtils.unescape(fieldCtx.identifier().getText());
        final JavaSootField f =
            new JavaSootField(
                identifierFactory.getFieldSignature(fieldName, clazz, fieldCtx.type().getText()),
                modifier,
                pos);
        if (fields.stream().anyMatch(e -> e.getName().equals(fieldName))) {
          throw new ResolveException("Field with the same name does already exist.", path, pos);
        } else {
          fields.add(f);
        }
      }
    }

    return true;
  }

  EnumSet<ClassModifier> getClassModifiers(List<JimpleParser.Class_modifierContext> modifier) {
    return modifier.stream()
        .map(modContext -> ClassModifier.valueOf(modContext.getText().toUpperCase()))
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(ClassModifier.class)));
  }

  EnumSet<MethodModifier> getMethodModifiers(List<JimpleParser.Method_modifierContext> modifier) {
    return modifier.stream()
        .map(
            modContext -> {
              // we need the following check, as old Soot wrongfully mapped VARARGS method
              // modifiers to TRANSIENT modifiers
              if (modContext.getText().equalsIgnoreCase("TRANSIENT")) {
                return MethodModifier.valueOf("VARARGS");
              }
              return MethodModifier.valueOf(modContext.getText().toUpperCase());
            })
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(MethodModifier.class)));
  }

  EnumSet<FieldModifier> getFieldModifiers(List<JimpleParser.Field_modifierContext> modifier) {
    return modifier.stream()
        .map(modContext -> FieldModifier.valueOf(modContext.getText().toUpperCase()))
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(FieldModifier.class)));
  }
}
