package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Ashik Mogasavara Ravikumar
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

/**
 * Utility for serializing and deserializing call-graph snapshots using FST.
 *
 * <p>This class is a thin wrapper around the FST (Fast-Serialization) library to persist a compact
 * snapshot of a call graph. The serializer writes/reads a DTO representation (CgSnapshot) that
 * contains arrays and primitive types for fast stable serialization.
 */
public class CallGraphFSTSerializer {

  private CallGraphFSTSerializer() {}

  /**
   * FSTConfiguration is not always thread-safe for some operations so we keep one per-thread here.
   * We also register the DTO classes used for serialization to speed up FST.
   */
  private static final ThreadLocal<FSTConfiguration> CONF =
      ThreadLocal.withInitial(
          () -> {
            FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
            conf.setPreferSpeed(true);
            conf.setShareReferences(false);
            conf.registerClass(
                CgSnapshot.class, String.class, int[].class, String[].class, ArrayList.class);
            return conf;
          });

  /**
   * Serialize the provided call graph to the given file path using FST.
   *
   * <p>The method converts the in-memory call graph into a compact DTO snapshot and writes it to
   * the file at {@code path}.
   *
   * @param cg the call graph to serialize
   * @param path destination file path for the serialized snapshot
   * @throws IOException on I/O or serialization errors
   */
  public static void write(CallGraph cg, Path path) throws IOException {
    SnapshotBuilder snap = buildSnapshotFromCalls(cg);
    if (snap != null) {
      try (OutputStream fileOut = new BufferedOutputStream(Files.newOutputStream(path));
          FSTObjectOutput o = new FSTObjectOutput(fileOut, CONF.get())) {
        o.writeObject(snap.toDto());
        o.flush();
      } catch (Exception e) {
        throw new IOException("Failed to write call graph snapshot", e);
      }
    } else {
      throw new RuntimeException(
          "Unsupported CallGraph implementation for serialization: " + cg.getClass());
    }
  }

  /**
   * Read a call-graph snapshot from the provided input stream and rebuild a {@link
   * MutableCallGraph} using the provided {@link View} to resolve method signatures back to method
   * objects.
   *
   * <p>The reader will recreate methods in the original order and attempt to match invocation-site
   * strings + line numbers to statements in the resolved method bodies.
   *
   * @param in input stream containing an FST-serialized {@link CgSnapshot}
   * @param view view used to parse and resolve method signatures
   * @return reconstructed mutable call graph
   * @throws IOException on I/O or deserialization errors
   */
  public static MutableCallGraph read(InputStream in, View view) throws IOException {
    FSTConfiguration conf = CONF.get();

    try (FSTObjectInput oi = new FSTObjectInput(in, conf)) {
      Object o = oi.readObject(CgSnapshot.class);
      CgSnapshot dto = (CgSnapshot) o;

      // Rebuild method signatures
      MethodSignature[] methods = new MethodSignature[dto.methods.length];

      for (int i = 0; i < methods.length; i++) {
        methods[i] = view.getIdentifierFactory().parseMethodSignature(dto.methods[i]);
      }

      MutableCallGraph rebuildGraph = new GraphBasedCallGraph(Arrays.stream(methods).toList());
      for (MethodSignature m : methods) {
        rebuildGraph.addMethod(m);
      }

      for (int i = 0; i < dto.src.length; i++) {
        MethodSignature srcMethod = methods[dto.src[i]];
        MethodSignature tgtMethod = methods[dto.tgt[i]];
        String wantedStmt = dto.invStmt[i];
        int ln = dto.stmtLine[i];

        var mOpt = view.getMethod(srcMethod);
        if (!mOpt.isPresent()) continue;
        List<Stmt> stmts = mOpt.get().getBody().getStmts();
        InvokableStmt chosenStmt = null;

        if (!wantedStmt.isBlank() && ln >= 0) {
          for (Stmt s : stmts) {
            var sp = s.getPositionInfo().getStmtPosition();
            InvokableStmt inv = invokableIfAny(s);
            if (inv != null && wantedStmt.equals(inv.toString()) && sp.getFirstLine() == ln) {
              chosenStmt = inv;
              break;
            }
          }
        }

        if (chosenStmt != null) rebuildGraph.addCall(srcMethod, tgtMethod, chosenStmt);
      }
      return rebuildGraph;
    } catch (Exception e) {
      throw new IOException("Failed to read call graph snapshot", e);
    }
  }

  public static MutableCallGraph read(Path path, View view) throws IOException {
    try (InputStream in = Files.newInputStream(path)) {
      return read(in, view);
    } catch (Exception e) {
      throw new IOException("Failed to read call graph snapshot from file: " + path, e);
    }
  }

  /**
   * Convert the supported call graph implementation into a compact snapshot builder object. The
   * snapshot contains methods and arrays of indices for source/target methods plus invocation-site
   * information.
   *
   * @param cg call graph to convert
   * @return snapshot builder
   */
  private static SnapshotBuilder buildSnapshotFromCalls(CallGraph cg) {
    List<CallGraph.Call> calls;
    if (cg instanceof MutableCallGraph g) {
      calls = g.getCalls().stream().toList();
    } else {
      return null;
    }

    LinkedHashMap<String, Integer> callIndexMap = new LinkedHashMap<>();
    List<Integer> src = new ArrayList<>(calls.size());
    List<Integer> tgt = new ArrayList<>(calls.size());
    List<String> invStmt = new ArrayList<>(calls.size());
    List<Integer> stmtLine = new ArrayList<>(calls.size());

    for (CallGraph.Call c : calls) {
      String sTxt = c.sourceMethodSignature().toString();
      String tTxt = c.targetMethodSignature().toString();

      // computeIfAbsent ensures each unique method signature is assigned
      // a stable integer index used in the serialized arrays.
      int si = callIndexMap.computeIfAbsent(sTxt, k -> callIndexMap.size());
      int ti = callIndexMap.computeIfAbsent(tTxt, k -> callIndexMap.size());

      src.add(si);
      tgt.add(ti);

      InvokableStmt inv = c.invokableStmt();
      int line = -1;
      if (inv.getPositionInfo() != null) {
        var sp = inv.getPositionInfo().getStmtPosition();
        line = sp.getFirstLine();
      }
      // Store a textual representation of the invocation site and the
      // statement line number so the reader can attempt to
      // match the exact statement later.
      invStmt.add(inv.toString());
      stmtLine.add(line);
    }

    String[] methods = callIndexMap.keySet().toArray(new String[0]);
    return new SnapshotBuilder(
        methods,
        src.stream().mapToInt(Integer::intValue).toArray(),
        tgt.stream().mapToInt(Integer::intValue).toArray(),
        invStmt.toArray(new String[0]),
        stmtLine.stream().mapToInt(Integer::intValue).toArray());
  }

  /**
   * Helper that returns the invokable form of a statement if it represents an invocation, otherwise
   * returns null. This method helps avoid unexpected failures and keep deserialization resilient.
   */
  private static InvokableStmt invokableIfAny(Stmt s) {
    try {
      if (s instanceof JInvokeStmt) {
        return s.asInvokableStmt();
      }
      if (s instanceof JAssignStmt) {
        return s.asInvokableStmt();
      }
    } catch (Throwable ignore) {
      return null;
    }
    return null;
  }

  private record SnapshotBuilder(
      String[] methods, int[] src, int[] tgt, String[] invStmt, int[] stmtLine) {
    // Convert the builder to the serializable DTO used by the FST writer
    CgSnapshot toDto() {
      return new CgSnapshot(1, methods, src, tgt, invStmt, stmtLine);
    }
  }

  /**
   * DTO stored in the serialized stream. Keep fields package-private for simplicity; FST will
   * serialize them directly.
   */
  static class CgSnapshot implements Serializable {
    final int version;
    final String[] methods;
    final int[] src, tgt;

    final String[] invStmt;
    final int[] stmtLine;

    CgSnapshot(
        int version, String[] methods, int[] src, int[] tgt, String[] invStmt, int[] stmtLine) {
      this.version = version;
      this.methods = methods;
      this.src = src;
      this.tgt = tgt;
      this.invStmt = invStmt;
      this.stmtLine = stmtLine;
    }
  }
}
