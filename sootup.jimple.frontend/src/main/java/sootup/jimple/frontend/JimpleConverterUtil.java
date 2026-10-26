package sootup.jimple.frontend;

/*-
 * #%L
 * SootUp
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
import org.antlr.v4.runtime.*;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ResolveException;
import sootup.core.jimple.JimpleUtils;
import sootup.core.jimple.common.Trap;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.FullPosition;
import sootup.core.model.Position;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.types.JavaClassType;
import sootup.jimple.JimpleLexer;
import sootup.jimple.JimpleParser;

/**
 * This Utility class provides common used methods in context with parsing Jimple.
 *
 * @author Markus Schmidt
 */
public class JimpleConverterUtil {

  @NonNull private final IdentifierFactory identifierFactory;
  private final Map<String, PackageName> imports = new HashMap<>();
  @NonNull private final Path fileUri;

  public JimpleConverterUtil(@NonNull Path file, @NonNull IdentifierFactory identifierFactory) {
    this.fileUri = file;
    this.identifierFactory = identifierFactory;
  }

  public IdentifierFactory getIdentifierFactory() {
    return identifierFactory;
  }

  public Type getType(String typename) {
    typename = JimpleUtils.unescape(typename);
    PackageName packageName = imports.get(typename);
    return packageName == null
        ? identifierFactory.getType(typename)
        : identifierFactory.getType(packageName.getName() + "." + typename);
  }

  public ClassType getClassType(String typename) {
    typename = JimpleUtils.unescape(typename);
    PackageName packageName = this.imports.get(typename);
    return packageName == null
        ? this.identifierFactory.getClassType(typename)
        : this.identifierFactory.getClassType(typename, packageName.getName());
  }

  @NonNull
  public static Position buildPositionFromCtx(@NonNull ParserRuleContext ctx) {

    // calc end position (line number+char offset in line) as antlr is not capable to do it
    // intuitively
    String tokenstr = ctx.getText();
    // lsp indexes everything zero based - antlr does it only chars in line; linenos are indexed
    // one-based => subtract one ;)
    int lineCount = -1;
    int fromIdx = 0;
    int lastLineBreakIdx = 0;
    while ((fromIdx = tokenstr.indexOf("\n", fromIdx)) != -1) {
      lastLineBreakIdx = fromIdx;
      lineCount++;
      fromIdx++;
    }

    int endCharLength = tokenstr.length() - lastLineBreakIdx;

    return new FullPosition(
        ctx.start.getLine() - 1,
        ctx.start.getCharPositionInLine(),
        ctx.stop.getLine() + lineCount,
        ctx.stop.getCharPositionInLine() + endCharLength);
  }

  public void addImport(JimpleParser.ImportItemContext item) {
    if (item == null || item.location == null) {
      return;
    }
    final ClassType classType =
        identifierFactory.getClassType(JimpleUtils.unescape(item.location.getText()));
    final PackageName duplicate =
        imports.putIfAbsent(classType.getClassName(), classType.getPackageName());
    if (duplicate != null && !duplicate.equals(classType.getPackageName())) {
      throw new ResolveException(
          "Multiple Imports for the same ClassName can not be resolved!",
          fileUri,
          buildPositionFromCtx(item));
    }
  }

  @NonNull
  public MethodSignature getMethodSignature(
      JimpleParser.Method_signatureContext ctx, ParserRuleContext parentCtx) {
    if (ctx == null) {
      throw new ResolveException(
          "MethodSignature is missing.", fileUri, buildPositionFromCtx(parentCtx));
    }

    JimpleParser.IdentifierContext class_name = ctx.class_name;
    if (class_name == null) {
      throw new ResolveException(
          "MethodSignature is not well formed.", fileUri, buildPositionFromCtx(ctx));
    }

    final MethodSubSignature methodSubSignature =
        getMethodSubSignature(ctx.method_subsignature(), ctx);
    String classname = class_name.getText();
    return identifierFactory.getMethodSignature(getClassType(classname), methodSubSignature);
  }

  @NonNull
  public MethodSubSignature getMethodSubSignature(
      JimpleParser.Method_subsignatureContext ctx, ParserRuleContext parentCtx) {
    if (ctx == null) {
      throw new ResolveException(
          "MethodSignature is missing.", fileUri, buildPositionFromCtx(parentCtx));
    }
    JimpleParser.TypeContext typeCtx = ctx.type();
    JimpleParser.Method_nameContext method_nameCtx = ctx.method_name();
    if (typeCtx == null || method_nameCtx == null) {
      throw new ResolveException(
          "MethodSignature is not well formed.", fileUri, buildPositionFromCtx(ctx));
    }
    Type type = getType(typeCtx.getText());
    String methodname = JimpleUtils.unescape(method_nameCtx.getText());
    List<Type> params = getTypeList(ctx.type_list());
    return identifierFactory.getMethodSubSignature(methodname, type, params);
  }

  public FieldSignature getFieldSignature(JimpleParser.Field_signatureContext ctx) {
    String classname = JimpleUtils.unescape(ctx.classname.getText());
    Type type = getType(JimpleUtils.unescape(ctx.type().getText()));
    String fieldname = JimpleUtils.unescape(ctx.fieldname.getText());
    return identifierFactory.getFieldSignature(fieldname, getClassType(classname), type);
  }

  public List<Type> getTypeList(JimpleParser.Type_listContext type_list) {
    if (type_list == null) {
      return Collections.emptyList();
    }
    List<JimpleParser.TypeContext> typeList = type_list.type();
    int size = typeList.size();
    if (size < 1) {
      return Collections.emptyList();
    }
    List<Type> list = new ArrayList<>(size);
    for (JimpleParser.TypeContext typeContext : typeList) {
      list.add(identifierFactory.getType(JimpleUtils.unescape(typeContext.getText())));
    }
    return list;
  }

  public List<ClassType> getClassTypeList(JimpleParser.Type_listContext type_list) {
    if (type_list == null) {
      return Collections.emptyList();
    }
    List<JimpleParser.TypeContext> typeList = type_list.type();
    int size = typeList.size();
    if (size < 1) {
      return Collections.emptyList();
    }
    List<ClassType> list = new ArrayList<>(size);
    for (JimpleParser.TypeContext typeContext : typeList) {
      list.add(identifierFactory.getClassType(JimpleUtils.unescape(typeContext.getText())));
    }
    return list;
  }

  public Set<JavaClassType> getClassTypeSet(JimpleParser.Type_listContext type_list) {
    if (type_list == null) {
      return Collections.emptySet();
    }
    final List<JimpleParser.TypeContext> typeList = type_list.type();
    final int size = typeList.size();
    if (size < 1) {
      return Collections.emptySet();
    }
    Set<JavaClassType> set = new HashSet<>(size);
    for (JimpleParser.TypeContext typeContext : typeList) {
      set.add(
          (JavaClassType)
              identifierFactory.getClassType(JimpleUtils.unescape(typeContext.getText())));
    }
    return set;
  }

  /**
   * Removes the Blocks that are not reachable from the first Block - neither via fallthrough, a
   * branch nor via the exceptional flow of a Trap covering a reachable Block. A ControlFlowGraph can
   * not contain such unreachable Stmts, but serialized Jimple can - e.g. the JimplePrinter appends an
   * artificial nop as the last Stmt to denote the (exclusive) end of a Trap that covers the last
   * Stmt of the body.
   *
   * <p>The Traps are adapted accordingly: a Trap range shrinks to its reachable Blocks i.e. it begins
   * at the first reachable Block in its range and ends at the next reachable Block after its range
   * (or at the end of the body, denoted by an endStmt that is in no Block). A Trap without any
   * reachable Block in its range is removed.
   *
   * @param blocks the linearized Blocks; the head of each Block is either the first Stmt, a labeled
   *     Stmt or follows a branching Stmt
   * @param traps the Traps whose begin, end and handler Stmts are Block heads
   * @param labeledStmts maps label names to their Stmt
   * @param unresolvedBranches maps each BranchingStmt to its target labels
   */
  public static void removeUnreachableBlocks(
      @NonNull List<List<Stmt>> blocks,
      @NonNull List<Trap> traps,
      @NonNull Map<String, Stmt> labeledStmts,
      @NonNull Map<BranchingStmt, List<String>> unresolvedBranches) {
    final int blockCount = blocks.size();
    if (blockCount == 0) {
      return;
    }

    final Map<Stmt, Integer> headToBlockIdx = new IdentityHashMap<>();
    for (int i = 0; i < blockCount; i++) {
      headToBlockIdx.put(blocks.get(i).get(0), i);
    }

    // trap ranges as [beginIdx, endIdx) of Block indices, handlerIdx
    final int[][] trapRanges = new int[traps.size()][];
    for (int t = 0; t < traps.size(); t++) {
      final Trap trap = traps.get(t);
      final Integer beginIdx = headToBlockIdx.get(trap.getBeginStmt());
      final Integer endIdx = headToBlockIdx.get(trap.getEndStmt());
      final Integer handlerIdx = headToBlockIdx.get(trap.getHandlerStmt());
      if (beginIdx == null || endIdx == null || handlerIdx == null) {
        // invalid Trap: leave it to the ControlFlowGraph creation to report it
        return;
      }
      trapRanges[t] = new int[] {beginIdx, endIdx, handlerIdx};
    }

    // determine reachable Blocks
    final boolean[] reachable = new boolean[blockCount];
    final Deque<Integer> worklist = new ArrayDeque<>();
    reachable[0] = true;
    worklist.add(0);
    while (!worklist.isEmpty()) {
      final int blockIdx = worklist.poll();
      final List<Integer> successorIdxs = new ArrayList<>();

      final List<Stmt> block = blocks.get(blockIdx);
      final Stmt tail = block.get(block.size() - 1);
      if (tail instanceof FallsThroughStmt && blockIdx + 1 < blockCount) {
        successorIdxs.add(blockIdx + 1);
      }
      if (tail instanceof BranchingStmt) {
        final List<String> targetLabels = unresolvedBranches.get(tail);
        if (targetLabels != null) {
          for (String targetLabel : targetLabels) {
            final Integer targetIdx = headToBlockIdx.get(labeledStmts.get(targetLabel));
            if (targetIdx != null) {
              successorIdxs.add(targetIdx);
            }
          }
        }
      }
      for (int[] trapRange : trapRanges) {
        if (trapRange[0] <= blockIdx && blockIdx < trapRange[1]) {
          successorIdxs.add(trapRange[2]);
        }
      }

      for (int successorIdx : successorIdxs) {
        if (!reachable[successorIdx]) {
          reachable[successorIdx] = true;
          worklist.add(successorIdx);
        }
      }
    }

    boolean allReachable = true;
    for (boolean isReachable : reachable) {
      allReachable &= isReachable;
    }
    if (allReachable) {
      return;
    }

    // adapt the Trap ranges to the reachable Blocks
    final List<Trap> adaptedTraps = new ArrayList<>(traps.size());
    for (int t = 0; t < traps.size(); t++) {
      final Trap trap = traps.get(t);
      final int beginIdx = trapRanges[t][0];
      final int endIdx = trapRanges[t][1];

      int newBeginIdx = beginIdx;
      while (newBeginIdx < endIdx && !reachable[newBeginIdx]) {
        newBeginIdx++;
      }
      if (newBeginIdx >= endIdx) {
        // the Trap covers no reachable Stmt
        continue;
      }

      int newEndIdx = endIdx;
      while (newEndIdx < blockCount && !reachable[newEndIdx]) {
        newEndIdx++;
      }
      // no reachable Block after the range: the removed endStmt denotes the end of the body
      final Stmt newEndStmt =
          newEndIdx < blockCount ? blocks.get(newEndIdx).get(0) : trap.getEndStmt();

      adaptedTraps.add(
          new Trap(
              trap.getExceptionType(),
              blocks.get(newBeginIdx).get(0),
              newEndStmt,
              trap.getHandlerStmt()));
    }
    traps.clear();
    traps.addAll(adaptedTraps);

    // remove the unreachable Blocks
    for (int i = blockCount - 1; i >= 0; i--) {
      if (!reachable[i]) {
        final List<Stmt> removedBlock = blocks.remove(i);
        unresolvedBranches.remove(removedBlock.get(removedBlock.size() - 1));
      }
    }
  }

  @NonNull
  public static JimpleParser createJimpleParser(CharStream charStream, Path path) {
    JimpleLexer lexer = new JimpleLexer(charStream);

    lexer.removeErrorListeners();
    lexer.addErrorListener(
        new BaseErrorListener() {
          @Override
          public void syntaxError(
              Recognizer<?, ?> recognizer,
              Object offendingSymbol,
              int line,
              int charPositionInLine,
              String msg,
              RecognitionException e) {

            throw new ResolveException(
                "Jimple SyntaxError: " + msg,
                path,
                new FullPosition(line - 1, charPositionInLine, line - 1, Integer.MAX_VALUE));
          }
        });

    TokenStream tokens = new CommonTokenStream(lexer);
    JimpleParser parser = new JimpleParser(tokens);

    parser.removeErrorListeners();
    parser.addErrorListener(
        new BaseErrorListener() {
          @Override
          public void syntaxError(
              Recognizer<?, ?> recognizer,
              Object offendingSymbol,
              int line,
              int charPositionInLine,
              String msg,
              RecognitionException e) {

            Position position;
            if (e != null) {
              if (e.getCause() instanceof NoViableAltException) {
                Token start = ((NoViableAltException) e.getCause()).getStartToken();
                position =
                    new FullPosition(
                        start.getLine() - 1,
                        start.getCharPositionInLine(),
                        charPositionInLine - 1,
                        Integer.MAX_VALUE);
              } else {
                // hint: not precise if erroneous input spans across multiple lines!
                int sizeOfBad = e.getCtx().getText().length();
                int firstCol = Math.max(charPositionInLine - sizeOfBad, 0);
                position = new FullPosition(line - 1, firstCol, line - 1, Integer.MAX_VALUE);
              }
            } else {
              position =
                  new FullPosition(line - 1, charPositionInLine, line - 1, Integer.MAX_VALUE);
            }

            throw new ResolveException("Jimple SyntaxError: " + msg, path, position);
          }
        });
    return parser;
  }
}
