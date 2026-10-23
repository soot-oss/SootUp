package sootup.java.bytecode.frontend.conversion;

import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.NOP;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import sootup.core.inputlocation.AnalysisExtendedScope;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.LocalVariableStmtPositionInfo;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.LocalVariableInfo;
import sootup.core.model.LocalVariableScope;
import sootup.core.model.SourceType;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

/** Verifies scope capture, snapshot sharing, and propagation through bytecode input locations. */
class LocalVariableTableTest {
  /**
   * Scans nested ranges to check snapshot and binding sharing, inclusive starts, and exclusive
   * ends.
   */
  @Test
  void scopeSnapshotsAreSharedAndEndLabelsAreExclusive() {
    InsnList instructions = new InsnList();
    LabelNode start = new LabelNode(), innerStart = new LabelNode();
    LabelNode innerEnd = new LabelNode(), end = new LabelNode();
    InsnNode first = new InsnNode(NOP), second = new InsnNode(NOP), inner = new InsnNode(NOP);
    instructions.add(start);
    instructions.add(first);
    instructions.add(second);
    instructions.add(innerStart);
    instructions.add(inner);
    instructions.add(innerEnd);
    instructions.add(new InsnNode(NOP));
    instructions.add(end);
    instructions.add(new InsnNode(NOP));
    var locals =
        new LocalVariableTableLocals(
            List.of(
                new LocalVariableNode("outer", "I", null, start, end, 0),
                new LocalVariableNode("inner", "I", null, innerStart, innerEnd, 1)),
            instructions,
            instructions::indexOf,
            List.of());
    var scopes = locals.createScopes();
    assertSame(scopes.get(start), scopes.get(first));
    assertSame(scopes.get(first), scopes.get(second));
    assertSame(scopes.get(start), scopes.get(innerEnd));
    assertEquals(List.of(new LocalVariableInfo("outer", 0, "I")), scopes.get(first).getVariables());
    assertEquals(
        List.of(new LocalVariableInfo("outer", 0, "I"), new LocalVariableInfo("inner", 1, "I")),
        scopes.get(inner).getVariables());
    assertEquals(scopes.get(first).getVariables(), scopes.get(innerEnd).getVariables());
    assertSame(LocalVariableScope.empty(), scopes.get(end));
    assertSame(scopes.get(first).getVariables().get(0), scopes.get(inner).getVariables().get(0));
  }

  /** Checks adjacent equal entries share one snapshot and a zero-length entry adds no binding. */
  @Test
  void adjacentEntriesReusingTheSameNameAndSlotShareScopeByValue() {
    InsnList instructions = new InsnList();
    LabelNode start = new LabelNode(), boundary = new LabelNode(), end = new LabelNode();
    instructions.add(start);
    instructions.add(new InsnNode(NOP));
    instructions.add(boundary);
    instructions.add(new InsnNode(NOP));
    instructions.add(end);
    var locals =
        new LocalVariableTableLocals(
            List.of(
                new LocalVariableNode("x", "I", null, start, boundary, 1),
                new LocalVariableNode("x", "I", null, boundary, end, 1),
                new LocalVariableNode("empty", "I", null, boundary, boundary, 2)),
            instructions,
            instructions::indexOf,
            List.of());
    var scopes = locals.createScopes();
    assertSame(scopes.get(start), scopes.get(boundary));
    assertEquals(List.of(new LocalVariableInfo("x", 1, "I")), scopes.get(boundary).getVariables());
    assertSame(LocalVariableScope.empty(), scopes.get(end));
  }

  /**
   * Restarts two bindings across a gap and at a shared boundary, checking equal snapshots reuse the
   * original object.
   */
  @Test
  void equalMultiBindingScopesAreReusedAcrossAGapAndSimultaneousTransitions() {
    InsnList instructions = new InsnList();
    LabelNode firstStart = new LabelNode(), firstEnd = new LabelNode();
    LabelNode secondStart = new LabelNode(), transition = new LabelNode(), end = new LabelNode();
    for (LabelNode label : List.of(firstStart, firstEnd, secondStart, transition, end)) {
      instructions.add(label);
      instructions.add(new InsnNode(NOP));
    }
    var locals =
        new LocalVariableTableLocals(
            List.of(
                new LocalVariableNode("x", "I", null, firstStart, firstEnd, 0),
                new LocalVariableNode("x", "I", null, secondStart, transition, 0),
                new LocalVariableNode("x", "I", null, transition, end, 0),
                new LocalVariableNode("y", "J", null, firstStart, firstEnd, 1),
                new LocalVariableNode("y", "J", null, secondStart, transition, 1),
                new LocalVariableNode("y", "J", null, transition, end, 1)),
            instructions,
            instructions::indexOf,
            List.of());

    var scopes = locals.createScopes();

    LocalVariableScope first = scopes.get(firstStart);
    assertEquals(
        List.of(new LocalVariableInfo("x", 0, "I"), new LocalVariableInfo("y", 1, "J")),
        first.getVariables());
    assertSame(
        first, scopes.get(secondStart), "Equal lists from different entries reuse the snapshot");
    assertSame(
        first, scopes.get(transition), "Ending and starting both entries keeps the same snapshot");
    assertSame(first.getVariables().get(0), scopes.get(secondStart).getVariables().get(0));
    assertSame(first.getVariables().get(1), scopes.get(transition).getVariables().get(1));
    assertSame(LocalVariableScope.empty(), scopes.get(firstEnd));
    assertSame(scopes.get(firstEnd), scopes.get(end));
    assertEquals(instructions.size(), scopes.size());
  }

  /**
   * Retains both same-slot entries and restores the pooled outer snapshot when the inner range
   * ends.
   */
  @Test
  void overlappingSlotEntriesRestoreAndReuseTheEarlierScopeWhenInnerEntryEnds() {
    InsnList instructions = new InsnList();
    LabelNode start = new LabelNode(), innerStart = new LabelNode();
    LabelNode innerEnd = new LabelNode(), end = new LabelNode();
    for (LabelNode label : List.of(start, innerStart, innerEnd, end)) {
      instructions.add(label);
      instructions.add(new InsnNode(NOP));
    }
    var locals =
        new LocalVariableTableLocals(
            List.of(
                new LocalVariableNode("outer", "I", null, start, end, 0),
                new LocalVariableNode("inner", "J", null, innerStart, innerEnd, 0)),
            instructions,
            instructions::indexOf,
            List.of());

    var scopes = locals.createScopes();

    LocalVariableInfo outer = scopes.get(start).getVariables().get(0);
    assertEquals("outer", outer.name());
    assertEquals(
        List.of(outer, new LocalVariableInfo("inner", 0, "J")),
        scopes.get(innerStart).getVariables());
    assertSame(scopes.get(start), scopes.get(innerEnd));
    assertSame(outer, scopes.get(innerStart).getVariables().get(0));
    assertSame(outer, scopes.get(innerEnd).getVariables().get(0));
    assertSame(LocalVariableScope.empty(), scopes.get(end));
  }

  /**
   * Reverses two same-slot bindings at a boundary, checking snapshots preserve table order without
   * selecting a winner.
   */
  @Test
  void tableOrderIsPreservedForOverlappingSlotEntries() {
    InsnList instructions = new InsnList();
    LabelNode start = new LabelNode(), boundary = new LabelNode(), end = new LabelNode();
    for (LabelNode label : List.of(start, boundary, end)) {
      instructions.add(label);
      instructions.add(new InsnNode(NOP));
    }
    var locals =
        new LocalVariableTableLocals(
            List.of(
                new LocalVariableNode("a", "I", null, start, boundary, 0),
                new LocalVariableNode("b", "I", null, start, boundary, 0),
                new LocalVariableNode("b", "I", null, boundary, end, 0),
                new LocalVariableNode("a", "I", null, boundary, end, 0)),
            instructions,
            instructions::indexOf,
            List.of());

    var scopes = locals.createScopes();

    assertNotSame(scopes.get(start), scopes.get(boundary));
    assertEquals(
        List.of(new LocalVariableInfo("a", 0, "I"), new LocalVariableInfo("b", 0, "I")),
        scopes.get(start).getVariables());
    assertEquals(
        List.of(new LocalVariableInfo("b", 0, "I"), new LocalVariableInfo("a", 0, "I")),
        scopes.get(boundary).getVariables());
  }

  /**
   * Changes name, slot, or descriptor independently, checking each change yields a different
   * snapshot.
   */
  @Test
  void differentNamesSlotsAndDescriptorsDoNotShareSnapshots() {
    for (LocalVariableInfo changed :
        List.of(
            new LocalVariableInfo("y", 0, "I"),
            new LocalVariableInfo("x", 1, "I"),
            new LocalVariableInfo("x", 0, "J"))) {
      InsnList instructions = new InsnList();
      LabelNode start = new LabelNode(), boundary = new LabelNode(), end = new LabelNode();
      instructions.add(start);
      instructions.add(new InsnNode(NOP));
      instructions.add(boundary);
      instructions.add(new InsnNode(NOP));
      instructions.add(end);
      var locals =
          new LocalVariableTableLocals(
              List.of(
                  new LocalVariableNode("x", "I", null, start, boundary, 0),
                  new LocalVariableNode(
                      changed.name(),
                      changed.descriptor(),
                      null,
                      boundary,
                      end,
                      changed.slotIndex())),
              instructions,
              instructions::indexOf,
              List.of());

      var scopes = locals.createScopes();

      assertNotSame(scopes.get(start), scopes.get(boundary), changed.toString());
      assertEquals(List.of(new LocalVariableInfo("x", 0, "I")), scopes.get(start).getVariables());
      assertEquals(List.of(changed), scopes.get(boundary).getVariables());
    }
  }

  /**
   * Checks labels, line/frame nodes, and instructions share snapshots within a capture; empty
   * snapshots also share across captures.
   */
  @Test
  void unchangedScopeIncludesLineAndFrameNodesAndIsPooledOnlyWithinOneCapture() {
    InsnList instructions = new InsnList();
    LabelNode start = new LabelNode(), end = new LabelNode();
    LineNumberNode line = new LineNumberNode(10, start);
    FrameNode frame = new FrameNode(org.objectweb.asm.Opcodes.F_SAME, 0, null, 0, null);
    InsnNode instruction = new InsnNode(NOP);
    instructions.add(start);
    instructions.add(line);
    instructions.add(frame);
    instructions.add(instruction);
    instructions.add(end);
    var locals =
        new LocalVariableTableLocals(
            List.of(new LocalVariableNode("x", "I", null, start, end, 0)),
            instructions,
            instructions::indexOf,
            List.of());

    var first = locals.createScopes();
    var second = locals.createScopes();

    assertEquals(instructions.size(), first.size());
    for (AbstractInsnNode node : List.of(start, line, frame, instruction)) {
      assertSame(first.get(start), first.get(node));
    }
    assertNotSame(first.get(start), second.get(start));
    assertEquals(first.get(start).getVariables(), second.get(start).getVariables());
    assertSame(LocalVariableScope.empty(), first.get(end));
    assertSame(first.get(end), second.get(end));
  }

  /**
   * Supplies empty, reversed, and missing-label ranges, checking no invalid binding becomes active.
   */
  @Test
  void invalidRangesDoNotCreateBoundaryEventsOrLeaveVariablesActive() {
    InsnList instructions = new InsnList();
    LabelNode start = new LabelNode(), end = new LabelNode(), missing = new LabelNode();
    instructions.add(start);
    instructions.add(new InsnNode(NOP));
    instructions.add(end);
    var locals =
        new LocalVariableTableLocals(
            List.of(
                new LocalVariableNode("empty", "I", null, start, start, 0),
                new LocalVariableNode("reversed", "I", null, end, start, 1),
                new LocalVariableNode("missingStart", "I", null, missing, end, 2),
                new LocalVariableNode("missingEnd", "I", null, start, missing, 3)),
            instructions,
            instructions::indexOf,
            List.of());

    var scopes = locals.createScopes();

    assertEquals(instructions.size(), scopes.size());
    for (AbstractInsnNode node : instructions) {
      assertSame(LocalVariableScope.empty(), scopes.get(node));
    }
  }

  /**
   * Checks missing or empty LVTs yield no snapshots, preserving the distinction from captured empty
   * scopes.
   */
  @Test
  void missingOrEmptyTableReturnsNoSnapshotsRatherThanCapturedEmptyScopes() {
    InsnList instructions = new InsnList();
    instructions.add(new InsnNode(NOP));

    assertTrue(
        new LocalVariableTableLocals(null, instructions, instructions::indexOf, List.of())
            .createScopes()
            .isEmpty());
    assertTrue(
        new LocalVariableTableLocals(List.of(), instructions, instructions::indexOf, List.of())
            .createScopes()
            .isEmpty());
  }

  private final String directory = "src/test/resources/bugfixes/";

  /**
   * Loads slot-reuse bytecode with LVT capture enabled, checking parameter bindings and original
   * names, slots, and descriptors.
   */
  @Test
  public void testLocalVariableTableEntriesPopulated() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory,
            SourceType.Application,
            Collections.emptyList(),
            EnumSet.of(AnalysisExtendedScope.LocalVariableTable));

    JavaView view = new JavaView(inputLocation);
    JavaSootMethod method =
        view.getClass(view.getIdentifierFactory().getClassType("LocalNamesReusedSlots"))
            .get()
            .getMethod(
                "main",
                Collections.singletonList(
                    view.getIdentifierFactory().getType("java.lang.String[]")))
            .get();

    Body body = method.getBody();
    List<Stmt> stmts = body.getStmts();
    assertTrue(
        stmts.stream()
            .allMatch(stmt -> LocalVariableStmtPositionInfo.getLocalVariables(stmt) != null));

    Set<LocalVariableInfo> allVars =
        stmts.stream()
            .map(LocalVariableStmtPositionInfo::getLocalVariables)
            .filter(Objects::nonNull)
            .flatMap(scope -> scope.getVariables().stream())
            .collect(Collectors.toSet());

    Set<String> varNames =
        allVars.stream().map(LocalVariableInfo::name).collect(Collectors.toSet());
    assertTrue(
        varNames.containsAll(List.of("args", "a", "b", "c", "d")),
        "Expected all 5 variables (args, a, b, c, d) in scopes");

    // args parameter starts at method entry
    Stmt startingStmt = body.getControlFlowGraph().getStartingStmt();
    assertTrue(startingStmt instanceof JIdentityStmt);
    LocalVariableScope startScope = LocalVariableStmtPositionInfo.getLocalVariables(startingStmt);
    assertNotNull(startScope);
    assertEquals(
        List.of(new LocalVariableInfo("args", 0, "[Ljava/lang/String;")),
        startScope.getVariables());

    // a and c share slot 1 with different types in disjoint scopes
    LocalVariableInfo a =
        allVars.stream().filter(v -> v.name().equals("a")).findFirst().orElseThrow();
    assertEquals(1, a.slotIndex());
    assertEquals("LLocalNamesReusedSlots$Foo;", a.descriptor());

    LocalVariableInfo c =
        allVars.stream().filter(v -> v.name().equals("c")).findFirst().orElseThrow();
    assertEquals(1, c.slotIndex());
    assertEquals("LLocalNamesReusedSlots$Bar;", c.descriptor());

    assertNotSame(a, c);
  }

  /**
   * Loads two locals named i in different slots, checking debug metadata retains both original
   * names.
   */
  @Test
  public void testOneNameInMultipleSlotsRetainsOriginalNamesInLvt() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory,
            SourceType.Application,
            Collections.emptyList(),
            EnumSet.of(AnalysisExtendedScope.LocalVariableTable));

    JavaView view = new JavaView(inputLocation);
    JavaSootMethod method =
        view.getClass(view.getIdentifierFactory().getClassType("LocalNamesCollisions"))
            .get()
            .getMethod(
                "oneNameTwoSlots",
                Collections.singletonList(view.getIdentifierFactory().getType("int")))
            .get();

    Body body = method.getBody();
    List<Stmt> stmts = body.getStmts();

    Set<LocalVariableInfo> iVars =
        stmts.stream()
            .map(LocalVariableStmtPositionInfo::getLocalVariables)
            .filter(Objects::nonNull)
            .flatMap(scope -> scope.getVariables().stream())
            .filter(v -> v.name().equals("i"))
            .collect(Collectors.toSet());

    assertEquals(2, iVars.size(), "Both variables should retain original name 'i' in LVT metadata");
    Set<Integer> slots =
        iVars.stream().map(LocalVariableInfo::slotIndex).collect(Collectors.toSet());
    assertEquals(Set.of(2, 3), slots);
  }

  /**
   * Loads the fixture without opting into LVT capture, checking every statement has unavailable
   * metadata.
   */
  @Test
  public void testLocalVariableTableOmittedByDefault() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory, SourceType.Application, Collections.emptyList());

    JavaView view = new JavaView(inputLocation);
    JavaSootMethod method =
        view.getClass(view.getIdentifierFactory().getClassType("LocalNamesReusedSlots"))
            .get()
            .getMethod(
                "main",
                Collections.singletonList(
                    view.getIdentifierFactory().getType("java.lang.String[]")))
            .get();

    Body body = method.getBody();
    assertTrue(
        body.getStmts().stream()
            .allMatch(stmt -> LocalVariableStmtPositionInfo.getLocalVariables(stmt) == null));
  }

  /**
   * Runs the default interceptor pipeline, checking surviving statements have scopes and expected
   * debug names remain.
   */
  @Test
  void scopesSurviveTheDefaultInterceptorPipeline() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory,
            SourceType.Application,
            BytecodeBodyInterceptors.Default.getBodyInterceptors(),
            EnumSet.of(AnalysisExtendedScope.LocalVariableTable));
    JavaView view = new JavaView(inputLocation);
    Body body =
        view.getClass(view.getIdentifierFactory().getClassType("LocalNamesReusedSlots"))
            .orElseThrow()
            .getMethod("main", List.of(view.getIdentifierFactory().getType("java.lang.String[]")))
            .orElseThrow()
            .getBody();
    assertTrue(
        body.getStmts().stream()
            .allMatch(stmt -> LocalVariableStmtPositionInfo.getLocalVariables(stmt) != null));
    var names =
        body.getStmts().stream()
            .flatMap(
                stmt ->
                    LocalVariableStmtPositionInfo.getLocalVariables(stmt).getVariables().stream())
            .map(LocalVariableInfo::name)
            .collect(Collectors.toSet());
    assertTrue(names.containsAll(List.of("args", "a", "b", "c", "d")));
  }

  private static final Set<AnalysisExtendedScope> EXTENDED =
      EnumSet.of(AnalysisExtendedScope.LocalVariableTable);

  @TempDir Path tempDir;

  /**
   * Loads directory, JAR, and both WAR layouts, checking scopes survive delegation without
   * line-number metadata.
   */
  @Test
  void scopesReachBodiesThroughDirectoryJarAndWarDelegatesWithoutLineNumbers() throws Exception {
    byte[] classBytes = makeClass();
    Path directory = Files.createDirectory(tempDir.resolve("classes"));
    Files.write(directory.resolve("ScopeFixture.class"), classBytes);
    byte[] jarBytes = archive("ScopeFixture.class", classBytes);
    Path jar = Files.write(tempDir.resolve("fixture.jar"), jarBytes);
    Path warClasses =
        Files.write(
            tempDir.resolve("classes.war"),
            archive("WEB-INF/classes/ScopeFixture.class", classBytes));
    Path warLib =
        Files.write(tempDir.resolve("lib.war"), archive("WEB-INF/lib/fixture.jar", jarBytes));

    for (Path path : List.of(directory, jar, warClasses, warLib)) {
      try (AnalysisInputLocation location =
          PathBasedAnalysisInputLocation.create(
              path,
              SourceType.Application,
              BytecodeBodyInterceptors.Default.getBodyInterceptors(),
              List.of(),
              EXTENDED)) {
        JavaView view = new JavaView(location);
        var type = view.getIdentifierFactory().getClassType("ScopeFixture");
        var source = location.getClassSource(type, view).orElseThrow();
        assertEquals(
            EXTENDED, source.getAnalysisInputLocation().getExtendedScope(), path.toString());
        Body body =
            view.getClass(type)
                .orElseThrow()
                .getMethod("identity", List.of(view.getIdentifierFactory().getType("int")))
                .orElseThrow()
                .getBody();
        assertFalse(body.getStmts().isEmpty());
        for (Stmt stmt : body.getStmts()) {
          LocalVariableScope scope = LocalVariableStmtPositionInfo.getLocalVariables(stmt);
          assertNotNull(scope, path + ": " + stmt);
          assertEquals(List.of(new LocalVariableInfo("argument", 0, "I")), scope.getVariables());
          assertEquals(-1, stmt.getPositionInfo().getStmtPosition().getFirstLine());
        }
      }
    }
  }

  /**
   * Updates the catch identity's source line while retaining handler bindings and excluding the
   * local whose range starts after ASTORE.
   */
  @Test
  void caughtExceptionLineUpdateKeepsHandlerScope() throws Exception {
    Files.write(tempDir.resolve("ScopeFixture.class"), makeClass());
    JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                tempDir.toString(), SourceType.Application, List.of(), EXTENDED));
    Body body =
        view.getClass(view.getIdentifierFactory().getClassType("ScopeFixture"))
            .orElseThrow()
            .getMethod("catching", List.of(view.getIdentifierFactory().getType("int")))
            .orElseThrow()
            .getBody();

    JIdentityStmt handler =
        body.getStmts().stream()
            .filter(
                stmt ->
                    stmt instanceof JIdentityStmt identity
                        && identity.getRightOp() instanceof JCaughtExceptionRef)
            .map(stmt -> (JIdentityStmt) stmt)
            .findFirst()
            .orElseThrow();
    assertEquals(50, handler.getPositionInfo().getStmtPosition().getFirstLine());
    LocalVariableScope handlerScope = LocalVariableStmtPositionInfo.getLocalVariables(handler);
    assertNotNull(handlerScope);
    assertEquals(
        List.of(new LocalVariableInfo("argument", 0, "I")),
        handlerScope.getVariables(),
        "The catch variable starts after ASTORE");
    for (Stmt stmt : body.getStmts()) {
      assertNotNull(LocalVariableStmtPositionInfo.getLocalVariables(stmt), stmt.toString());
    }
    Set<String> names =
        body.getStmts().stream()
            .flatMap(
                stmt ->
                    LocalVariableStmtPositionInfo.getLocalVariables(stmt).getVariables().stream())
            .map(LocalVariableInfo::name)
            .collect(Collectors.toSet());
    assertEquals(Set.of("argument", "caught"), names);
  }

  private static byte[] archive(String entry, byte[] bytes) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try (JarOutputStream jar = new JarOutputStream(buffer)) {
      for (int slash = entry.indexOf('/'); slash >= 0; slash = entry.indexOf('/', slash + 1)) {
        jar.putNextEntry(new JarEntry(entry.substring(0, slash + 1)));
        jar.closeEntry();
      }
      jar.putNextEntry(new JarEntry(entry));
      jar.write(bytes);
      jar.closeEntry();
    }
    return buffer.toByteArray();
  }

  private static byte[] makeClass() {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "ScopeFixture", null, "java/lang/Object", null);
    MethodVisitor method =
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "identity", "(I)I", null, null);
    method.visitCode();
    Label start = new Label(), end = new Label();
    method.visitLabel(start);
    method.visitVarInsn(Opcodes.ILOAD, 0);
    method.visitInsn(Opcodes.IRETURN);
    method.visitLabel(end);
    method.visitLocalVariable("argument", "I", null, start, end, 0);
    method.visitMaxs(0, 0);
    method.visitEnd();

    method =
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "catching", "(I)V", null, null);
    method.visitCode();
    Label tryStart = new Label(), tryEnd = new Label(), handler = new Label();
    Label catchStart = new Label(), catchEnd = new Label();
    method.visitTryCatchBlock(tryStart, tryEnd, handler, "java/lang/Exception");
    method.visitLabel(tryStart);
    method.visitLineNumber(10, tryStart);
    method.visitInsn(Opcodes.NOP);
    method.visitLabel(tryEnd);
    method.visitInsn(Opcodes.RETURN);
    method.visitLabel(handler);
    method.visitLineNumber(50, handler);
    method.visitVarInsn(Opcodes.ASTORE, 1);
    method.visitLabel(catchStart);
    method.visitInsn(Opcodes.NOP);
    method.visitInsn(Opcodes.RETURN);
    method.visitLabel(catchEnd);
    method.visitLocalVariable("argument", "I", null, tryStart, catchEnd, 0);
    method.visitLocalVariable("caught", "Ljava/lang/Exception;", null, catchStart, catchEnd, 1);
    method.visitMaxs(0, 0);
    method.visitEnd();
    writer.visitEnd();
    return writer.toByteArray();
  }

  /**
   * Checks path-based and classpath input locations retain the explicitly requested LVT extended
   * scope.
   */
  @Test
  public void testExtendedScopeConfiguration() {
    Path path = Paths.get("src/test/resources/multi-release-jar/mrjar.jar");
    PathBasedAnalysisInputLocation location =
        PathBasedAnalysisInputLocation.create(
            path,
            SourceType.Application,
            Collections.emptyList(),
            Collections.emptyList(),
            EnumSet.of(AnalysisExtendedScope.LocalVariableTable));

    assertTrue(location.getExtendedScope().contains(AnalysisExtendedScope.LocalVariableTable));
    assertEquals(
        Collections.singleton(AnalysisExtendedScope.LocalVariableTable),
        location.getExtendedScope());

    JavaClassPathAnalysisInputLocation cpLocation =
        new JavaClassPathAnalysisInputLocation(
            path.toString(), EnumSet.of(AnalysisExtendedScope.LocalVariableTable));
    assertTrue(cpLocation.getExtendedScope().contains(AnalysisExtendedScope.LocalVariableTable));
  }
}
