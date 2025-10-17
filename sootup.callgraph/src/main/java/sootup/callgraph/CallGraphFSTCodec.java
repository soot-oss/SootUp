package sootup.callgraph;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Method;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallGraphFSTCodec {

    private CallGraphFSTCodec() {
    }

    private static final ThreadLocal<FSTConfiguration> CONF = ThreadLocal.withInitial(() -> {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.setPreferSpeed(true);
        conf.setShareReferences(false);
        conf.registerClass(CgSnapshotV1.class, String.class, int[].class, String[].class, ArrayList.class);
        return conf;
    });

    // Fallback, impl-agnostic edge extraction via DOT export (public API).
    // Matches:  " <sig1> " -> " <sig2> " [ ... ];
    private static final Pattern DOT_EDGE =
            Pattern.compile("^Call:(?<src><.+?>)\\s*->\\s*(?<tgt><.+?>)\\s*(?:via.*)?$");


//    Pattern.compile(
//            "^\\s*\"(?<src>.+?)\"\\s*->\\s*\"(?<tgt>.+?)\"\\s*(?:\\[.*?\\])?\\s*$"  //todo
//            );


    public static void write(CallGraph cg, OutputStream out) throws IOException {
        SnapshotBuilder snap = buildSnapshotFromDot(cg);
        FSTConfiguration conf = CONF.get();
        try (FSTObjectOutput o = new FSTObjectOutput(out, conf)) {
            o.writeObject(snap.toDto());
            o.flush();
        }
    }

    public static GraphBasedCallGraph read(InputStream in, View view) throws IOException {
        FSTConfiguration conf = CONF.get();
        // (FST also exposes getObjectInput(InputStream))
        try (FSTObjectInput oi = new FSTObjectInput(in, conf)) {
            Object o = oi.readObject(CgSnapshotV1.class);
            CgSnapshotV1 dto = (CgSnapshotV1) o;

            // Rebuild method signatures
            MethodSignature[] methods = new MethodSignature[dto.methods.length];

            for (int i = 0; i < methods.length; i++) {
                methods[i] = view.getIdentifierFactory().parseMethodSignature(dto.methods[i]);
            }

            // Rebuild a mutable call graph
            GraphBasedCallGraph g = new GraphBasedCallGraph(Arrays.stream(methods).toList());
            for (MethodSignature m : methods) {
                g.addMethod(m);
            }

            for (int i = 0; i < dto.src.length; i++) {
                MethodSignature srcMs = methods[dto.src[i]];
                MethodSignature tgtMs = methods[dto.tgt[i]];
                int wantedLine = dto.iArr[i];

                boolean expectSpecialInv = "<init>".equals(tgtMs.getName());
                boolean expectStaticInv = !expectSpecialInv && view.getMethod(tgtMs)
                        .map(SootMethod::isStatic)
                        .orElse(false);

                List<Stmt> stmts;
                if (view.getMethod(srcMs).isPresent()) {
                    stmts = view.getMethod(srcMs).get().getBody().getStmts();
                    InvokableStmt chosenStmt = null;
                    for (Stmt s : stmts) {
                        if (s.getPositionInfo().getStmtPosition().getFirstLine() != wantedLine) continue;

                        if (s instanceof JInvokeStmt) {
                            var ie = s.asJInvokeStmt().getInvokeExpr().get();
                            if (expectSpecialInv && ie instanceof JSpecialInvokeExpr) {
                                chosenStmt = s.asJInvokeStmt();
                                break;
                            } else if (expectStaticInv && ie instanceof JStaticInvokeExpr) {
                                chosenStmt = s.asJInvokeStmt();
                                break;
                            } else if (!expectStaticInv && !expectSpecialInv &&
                                    !(ie instanceof JStaticInvokeExpr)) {
                                chosenStmt = s.asJInvokeStmt();
                                break;
                            }
                        }

                        else if (s instanceof JAssignStmt) {
                            // only treat as invokable if RHS is an invoke
                            var rhsOpt = s.asJAssignStmt().getRightOp();
                            if (rhsOpt instanceof JStaticInvokeExpr) {
                                if (expectStaticInv) {
                                    chosenStmt = s.asJAssignStmt();
                                    break;
                                } else {
                                    chosenStmt = s.asJAssignStmt();
                                    break;
                                }
                            } else if (rhsOpt instanceof JStaticFieldRef) {
                                chosenStmt = s.asJAssignStmt();
                                break;
                            }
                        }

                    }
                    if (chosenStmt != null) {
                        g.addCall(srcMs, tgtMs, chosenStmt);
                    }
                }
            }

            return g;
        } catch (Exception e) {
            throw new IOException("Failed to read call graph snapshot", e);
        }
    }

    public static void write(CallGraph cg, Path path) throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            write(cg, out);
        }
    }

    public static GraphBasedCallGraph read(Path path, View view) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return read(in, view);
        }
    }


    private static SnapshotBuilder buildSnapshotFromDot(CallGraph cg) {
        LinkedHashMap<String, Integer> nodeIndex = new LinkedHashMap<>();
        ArrayList<Integer> src = new ArrayList<>();
        ArrayList<Integer> tgt = new ArrayList<>();
        ArrayList<Integer> istmts = new ArrayList<>();

//        cg.exportAsDot().forEach(line -> {
//            Matcher m = DOT_EDGE.matcher(line);
//            if (!m.matches()) return;
//            String s = m.group("src");
//            String t = m.group("tgt");
////            String st = m.group("stmt");
//            int si = nodeIndex.computeIfAbsent(s, k -> nodeIndex.size());
//            int ti = nodeIndex.computeIfAbsent(t, k -> nodeIndex.size());
////            int istmt = nodeIndex.computeIfAbsent(st, k -> nodeIndex.size());
//            src.add(si);
//            tgt.add(ti);
////            istmts.add(istmt);
//        });

        for (CallGraph.Call c : cg.getCalls()) {
            Matcher m = DOT_EDGE.matcher(c.toString());
            if (!m.matches()) continue;
            String s = m.group("src");
            String t = m.group("tgt");
            istmts.add(c.getLineNumber());
            int si = nodeIndex.computeIfAbsent(s, k -> nodeIndex.size());
            int ti = nodeIndex.computeIfAbsent(t, k -> nodeIndex.size());
            src.add(si);
            tgt.add(ti);
        }


        String[] methods = nodeIndex.keySet().toArray(new String[0]);
        int[] sArr = src.stream().mapToInt(Integer::intValue).toArray();
        int[] tArr = tgt.stream().mapToInt(Integer::intValue).toArray();
        int[] iArr = istmts.stream().mapToInt(Integer::intValue).toArray();


        return new SnapshotBuilder(1, methods, sArr, tArr, iArr);
    }

    private record SnapshotBuilder(int version, String[] methods, int[] src, int[] tgt, int[] iArr) {
        CgSnapshotV1 toDto() {
            return new CgSnapshotV1(1, methods, src, tgt, iArr);
        }
    }


    //    todo check if serializable is needed for the call graph class instaed of here
    static class CgSnapshotV1 implements Serializable {
        final int version;
        final String[] methods;
        final int[] src;
        final int[] tgt;
        final int[] iArr;

        CgSnapshotV1(int version, String[] methods, int[] src, int[] tgt, int[] iArr) {
            this.version = version;
            this.methods = methods;
            this.src = src;
            this.iArr = iArr;
            this.tgt = tgt;
        }
    }
}



