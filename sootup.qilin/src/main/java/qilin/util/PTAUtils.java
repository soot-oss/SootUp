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

package qilin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qilin.CoreConfig;
import qilin.core.PTA;
import qilin.core.PTAScene;
import qilin.core.VirtualCalls;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.context.ContextElement;
import qilin.core.context.ContextElements;
import qilin.core.pag.*;
import qilin.core.sets.PointsToSet;
import qilin.core.sets.PointsToSetInternal;
import qilin.util.queue.UniqueQueue;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.NumberedString;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphConstants;
import soot.util.dot.DotGraphNode;
import soot.util.queue.ChunkedQueue;
import soot.util.queue.QueueReader;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class PTAUtils {
    private static final Logger logger = LoggerFactory.getLogger(PTAUtils.class);
    static final String output_dir = CoreConfig.v().getOutConfig().outDir;
    static Map<String, Node> nodes = new TreeMap<>();
    private static final RefType clRunnable = RefType.v("java.lang.Runnable");

    public static Map<LocalVarNode, Set<AllocNode>> calcStaticThisPTS(PTA pta) {
        Map<LocalVarNode, Set<AllocNode>> pts = new HashMap<>();
        Set<SootMethod> workList = new HashSet<>();
        PAG pag = pta.getPag();
        // add all instance methods which potentially contain static call
        for (SootMethod method : pta.getNakedReachableMethods()) {
            if (PTAUtils.isFakeMainMethod(method) || !method.isPhantom() && !method.isStatic()) {
                MethodPAG srcmpag = pag.getMethodPAG(method);
                LocalVarNode thisRef = (LocalVarNode) srcmpag.nodeFactory().caseThis();
                final PointsToSet other = pta.reachingObjects(thisRef).toCIPointsToSet();

                for (final Unit u : srcmpag.getInvokeStmts()) {
                    final Stmt s = (Stmt) u;
                    InvokeExpr ie = s.getInvokeExpr();
                    if (ie instanceof StaticInvokeExpr) {
                        for (Iterator<Edge> it = pta.getCallGraph().edgesOutOf(u); it.hasNext(); ) {
                            Edge e = it.next();
                            SootMethod tgtmtd = e.tgt();
                            MethodPAG tgtmpag = pag.getMethodPAG(tgtmtd);
                            MethodNodeFactory tgtnf = tgtmpag.nodeFactory();
                            LocalVarNode tgtThisRef = (LocalVarNode) tgtnf.caseThis();
                            // create "THIS" ptr for static method
                            Set<AllocNode> addTo = pts.computeIfAbsent(tgtThisRef, k -> new HashSet<>());
                            boolean returnValue = false;
                            for (Iterator<AllocNode> itx = other.iterator(); itx.hasNext(); ) {
                                AllocNode node = itx.next();
                                if (addTo.add(node)) {
                                    returnValue = true;
                                }
                            }
                            if (returnValue) {
                                workList.add(tgtmtd);
                            }
                        }
                    }
                }
            }
        }
        while (!workList.isEmpty()) {
            SootMethod method = workList.iterator().next();
            workList.remove(method);
            MethodPAG srcmpag = pag.getMethodPAG(method);
            LocalVarNode thisRef = (LocalVarNode) srcmpag.nodeFactory().caseThis();
            final Set<AllocNode> other = pts.computeIfAbsent(thisRef, k -> new HashSet<>());

            for (final Unit u : srcmpag.getInvokeStmts()) {
                final Stmt s = (Stmt) u;
                InvokeExpr ie = s.getInvokeExpr();
                if (ie instanceof StaticInvokeExpr) {
                    for (Iterator<Edge> it = pta.getCallGraph().edgesOutOf(u); it.hasNext(); ) {
                        Edge e = it.next();
                        SootMethod tgtmtd = e.tgt();
                        MethodPAG tgtmpag = pag.getMethodPAG(tgtmtd);
                        MethodNodeFactory tgtnf = tgtmpag.nodeFactory();
                        LocalVarNode tgtThisRef = (LocalVarNode) tgtnf.caseThis();
                        // create "THIS" ptr for static method
                        Set<AllocNode> addTo = pts.computeIfAbsent(tgtThisRef, k -> new HashSet<>());
                        if (addTo.addAll(other)) {
                            workList.add(tgtmtd);
                        }
                    }
                }
            }
        }
        return pts;
    }

    public static Object getIR(Object sparkNode) {
        if (sparkNode instanceof VarNode) {
            return ((VarNode) sparkNode).getVariable();
        } else if (sparkNode instanceof AllocNode) {
            return ((AllocNode) sparkNode).getNewExpr();
        } else {// sparkField?
            return sparkNode;
        }
    }

    public static boolean mustAlias(PTA pta, VarNode v1, VarNode v2) {
        PointsToSet v1pts = pta.reachingObjects(v1).toCIPointsToSet();
        PointsToSet v2pts = pta.reachingObjects(v2).toCIPointsToSet();
        return v1pts.pointsToSetEquals(v2pts);
    }

    public static void printPts(PTA pta, PointsToSet pts) {
        final StringBuffer ret = new StringBuffer();
        for (Iterator<AllocNode> it = pts.iterator(); it.hasNext(); ) {
            AllocNode n = it.next();
            ret.append("\t").append(n).append("\n");
        }
        System.out.print(ret);
    }

    /**
     * dump callgraph to sootoutput/callgraph.dot
     */
    public static void dumpCallGraph(Iterable<Edge> callgraph, boolean appOnly) {
        String filename = "callgraph";
        DotGraph canvas = setDotGraph(filename);

        int mn = -1;
        Set<String> methodSet = new HashSet<>();
        List<String> methodList = new ArrayList<>();

        for (Edge edge : callgraph) {
            MethodOrMethodContext srcmtd = edge.getSrc();
            if (appOnly && !srcmtd.method().getDeclaringClass().isApplicationClass())
                continue;
            MethodOrMethodContext dstmtd = edge.getTgt();
            String srcName = srcmtd.toString();

            if (methodSet.add(srcName)) {
                canvas.drawNode(srcName).setLabel("" + ++mn);
                methodList.add(mn, srcName);
            }
            String dstName = dstmtd.toString();

            if (methodSet.add(dstName)) {
                canvas.drawNode(dstName).setLabel("" + ++mn);
                methodList.add(mn, dstName);
            }
            canvas.drawEdge(srcName, dstName);
        }

        plotDotGraph(canvas, filename);
        try {
            PrintWriter out = new PrintWriter(new File(output_dir, "callgraphnodes"));
            for (int i = 0; i < methodList.size(); i++)
                out.println(i + ":" + methodList.get(i));
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * slice a callgrph, put nodes &edges related to method into set1&set2
     */
    private static void slice(CallGraph callgraph, MethodOrMethodContext method, Set<MethodOrMethodContext> set1,
                              Set<Edge> set2) {
        for (Iterator<Edge> it = callgraph.edgesInto(method); it.hasNext(); ) {
            Edge edge = it.next();
            set2.add(edge);
            MethodOrMethodContext src = edge.getSrc();
            if (set1.add(src))
                slice(callgraph, src, set1, set2);
        }

    }

    /**
     * dump callgraph strench to method
     */
    public static void dumpSlicedCallGraph(CallGraph callgraph, MethodOrMethodContext method) {
        Set<MethodOrMethodContext> tgts = new HashSet<>();
        Set<Edge> edges = new HashSet<>();
        tgts.add(method);
        slice(callgraph, method, tgts, edges);

        dumpCallGraph(edges, false);
    }

    /**
     * dump callgraph from entry to the method
     */
    public static void dumpSlicedCallGraph2(CallGraph callgraph, MethodOrMethodContext method) {
        Set<Edge> edges = new HashSet<>();
        Set<MethodOrMethodContext> visited = new HashSet<>();
        Queue<MethodOrMethodContext> queue = new UniqueQueue<>();
        queue.add(method);
        while (!queue.isEmpty()) {
            MethodOrMethodContext front = queue.poll();
            for (Iterator<Edge> it = callgraph.edgesInto(front); it.hasNext(); ) {
                Edge edge = it.next();
                MethodOrMethodContext src = edge.getSrc();
                if (visited.add(src)) {
                    queue.add(src);
                    edges.add(edge);
                    break;
                }
            }
        }
        dumpCallGraph(edges, false);
    }

    public static boolean isUnresolved(Type type) {
        if (type instanceof ArrayType at) {
            type = at.getArrayElementType();
        }
        if (!(type instanceof RefType rt))
            return false;
//        if (!rt.hasSootClass()) {
//            return true;
//        }
        SootClass cl = rt.getSootClass();
        return cl.resolvingLevel() < SootClass.HIERARCHY;
    }

    public static boolean castNeverFails(Type src, Type dst) {
        if (dst == null)
            return true;
        if (dst == src)
            return true;
        if (src == null)
            return false;
        if (dst.equals(src))
            return true;
        if (src instanceof NullType)
            return true;
        if (src instanceof AnySubType)
            return true;
        if (dst instanceof NullType)
            return false;
        if (dst instanceof AnySubType)
            throw new RuntimeException("oops src=" + src + " dst=" + dst);
        return PTAScene.v().getOrMakeFastHierarchy().canStoreType(src, dst);
    }

    public static QueueReader<SootMethod> dispatch(Type type, VirtualCallSite site) {
        final ChunkedQueue<SootMethod> targetsQueue = new ChunkedQueue<>();
        final QueueReader<SootMethod> targets = targetsQueue.reader();
        if (site.kind() == Kind.THREAD && !PTAScene.v().getOrMakeFastHierarchy().canStoreType(type, clRunnable)) {
            return targets;
        }
        MethodOrMethodContext container = site.container();
        if (site.iie() instanceof SpecialInvokeExpr && site.kind() != Kind.THREAD) {
            SootMethod target = VirtualCalls.v().resolveSpecial((SpecialInvokeExpr) site.iie(), site.subSig(), container.method());
            // if the call target resides in a phantom class then
            // "target" will be null, simply do not add the target in that case
            if (target != null) {
                targetsQueue.add(target);
            }
        } else {
            Type mType = site.recNode().getType();
            VirtualCalls.v().resolve(type, mType, site.subSig(), container.method(), targetsQueue);
        }
        return targets;
    }

    public static boolean addWithTypeFiltering(PointsToSetInternal pts, Type type, Node node) {
        if (PTAUtils.castNeverFails(node.getType(), type)) {
            return pts.add(node.getNumber());
        }
        return false;
    }

    /**
     * dump pts to sootoutput/pts
     */
    public static void dumpPts(PTA pta, boolean appOnly) {
        try {
            PrintWriter file = new PrintWriter(new File(output_dir, "pts.txt"));
            file.println("Points-to results:");
            for (final ValNode vn : pta.getPag().getValNodes()) {
                if (!(vn instanceof VarNode)) {
                    continue;
                }
                SootClass clz = null;
                if (vn instanceof LocalVarNode) {
                    SootMethod sm = ((LocalVarNode) vn).getMethod();
                    if (sm != null) {
                        clz = sm.getDeclaringClass();
                    }
                } else if (vn instanceof GlobalVarNode gvn) {
                    clz = gvn.getDeclaringClass();
                } else if (vn instanceof ContextVarNode cv) {
                    VarNode varNode = cv.base();
                    if (varNode instanceof LocalVarNode cvbase) {
                        clz = cvbase.getMethod().getDeclaringClass();
                    } else if (varNode instanceof GlobalVarNode gvn) {
                        clz = gvn.getDeclaringClass();
                    }
                }
                if (appOnly && clz != null && !clz.isApplicationClass()) {
                    continue;
                }

                String label = getNodeLabel(vn);
                nodes.put("[" + label + "]", vn);
                file.print(label + " -> {");
                PointsToSet p2set = pta.reachingObjects(vn);

                if (p2set == null || p2set.isEmpty()) {
                    file.print(" empty }\n");
                    continue;
                }
                for (Iterator<AllocNode> it = p2set.iterator(); it.hasNext(); ) {
                    Node n = it.next();
                    label = getNodeLabel(n);
                    nodes.put("[" + label + "]", n);
                    file.print(" ");
                    file.print(label);
                }
                file.print(" }\n");
            }
            dumpNodeNames(file);
            file.close();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't dump solution." + e);
        }
    }

    public static void slicePAG(AllocNode allocNode, PAG pag, String filename) {
        Map<Node, Set<Node>> edges = new HashMap<>();
        Queue<ValNode> queue = new UniqueQueue<>();
        Set<VarNode> tov = pag.allocLookup(allocNode);
        edges.computeIfAbsent(allocNode, k -> new HashSet<>()).addAll(tov);
        queue.addAll(tov);
        Set<Node> visited = new HashSet<>(tov);
        while (!queue.isEmpty()) {
            ValNode front = queue.poll();
            Set<ValNode> tovx = pag.simpleLookup(front);
            Set<Node> toex = edges.computeIfAbsent(front, k -> new HashSet<>());
            for (ValNode vn : tovx) {
                if (toex.add(vn)) {
                    queue.add(vn);
                    visited.add(vn);
                }
            }
        }
        DotGraph canvas = setDotGraph("hello");
        edges.forEach((n, elements) -> {
            drawNode(canvas, n);
            elements.forEach(element -> {
                drawNode(canvas, element);
                drawEdge(canvas, n, element, "black");
            });
        });
        canvas.plot(filename);
        for (Node n : visited) {
            System.out.println(n);
        }
    }

    public static void dumpMPAG(PAG pag, SootMethod method) {
        String filename = method.getSignature() + ".dot";
        DotGraph canvas = setDotGraph(filename);
        QueueReader<Node> reader = pag.getMethodPAG(method).getInternalReader().clone();
        while (reader.hasNext()) {
            Node src = reader.next();
            Node dst = reader.next();
            drawNode(canvas, src);
            drawNode(canvas, dst);
            String color = src instanceof AllocNode ? "green" : // alloc
                    src instanceof FieldRefNode ? "red" : // load
                            dst instanceof FieldRefNode ? "blue" : // store
                                    "black"; // simple
            drawEdge(canvas, src, dst, color);
        }
        plotDotGraph(canvas, filename);
    }

    /**
     * dump mPAGs to sootoutput/@filename.dot
     */
    public static void dumpMPAGs(PTA pta, String filename) {
        DotGraph canvas = setDotGraph(filename);
        for (SootMethod m : pta.getNakedReachableMethods()) {
            QueueReader<Node> reader = pta.getPag().getMethodPAG(m).getInternalReader().clone();
            while (reader.hasNext()) {
                Node src = reader.next();
                Node dst = reader.next();
                drawNode(canvas, src);
                drawNode(canvas, dst);
                String color = src instanceof AllocNode ? "green" : // alloc
                        src instanceof FieldRefNode ? "red" : // load
                                dst instanceof FieldRefNode ? "blue" : // store
                                        "black"; // simple
                drawEdge(canvas, src, dst, color);
            }
        }
        plotDotGraph(canvas, filename);
    }

    /**
     * dump pag to sootoutput/@filename.dot
     */
    public static void dumpPAG(PAG pag, String filename) {
        DotGraph canvas = setDotGraph(filename);

        // draw edges
        drawPAGMap(canvas, pag.getAlloc(), "green");
        drawPAGMap(canvas, pag.getSimple(), "black");
        drawInvPAGMap(canvas, pag.getStoreInv(), "blue");
        drawPAGMap(canvas, pag.getLoad(), "red");
        // collect nodes.
        Set<Node> nodes = new HashSet<>();
        pag.getAlloc().forEach((k, v) -> {
            nodes.add(k);
            nodes.addAll(v);
        });
        pag.getSimple().forEach((k, v) -> {
            nodes.add(k);
            nodes.addAll(v);
        });
        pag.getStoreInv().forEach((k, v) -> {
            nodes.add(k);
            nodes.addAll(v);
        });
        pag.getLoad().forEach((k, v) -> {
            nodes.add(k);
            nodes.addAll(v);
        });
        // draw nodes.
        nodes.forEach(node -> drawNode(canvas, node));
        plotDotGraph(canvas, filename);
    }

    private static void plotDotGraph(DotGraph canvas, String filename) {
        canvas.plot(output_dir + "/" + filename + ".dot");
    }

    private static DotGraph setDotGraph(String fileName) {
        DotGraph canvas = new DotGraph(fileName);
        canvas.setNodeShape(DotGraphConstants.NODE_SHAPE_BOX);
        canvas.setGraphLabel(fileName);
        return canvas;
    }

    public static String getNodeLabel(Node node) {
        int num = node.getNumber();
        if (node instanceof LocalVarNode)
            return "L" + num;
        else if (node instanceof ContextVarNode) {
            return "L" + num;
        } else if (node instanceof GlobalVarNode)
            return "G" + num;
        else if (node instanceof ContextField)
            return "OF" + num;
        else if (node instanceof FieldRefNode)
            return "VF" + num;
        else if (node instanceof AllocNode)
            return "O" + num;
        else
            throw new RuntimeException("no such node type exists!");
    }

    private static void drawNode(DotGraph canvas, Node node) {
        DotGraphNode dotNode = canvas.drawNode(node.toString());
        dotNode.setLabel("[" + getNodeLabel(node) + "]");
        nodes.put("[" + getNodeLabel(node) + "]", node);
    }

    private static void drawEdge(DotGraph canvas, Node src, Node dst, String color) {
        canvas.drawEdge(src.toString(), dst.toString()).setAttribute("color", color);
    }

    private static void dumpNodeNames(PrintWriter file) {
        nodes.forEach((l, n) -> file.println(l + n));
    }

    public static void dumpNodeNames(String fileName) {
        try {
            PrintWriter out = new PrintWriter(new File(output_dir, fileName));
            dumpNodeNames(out);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void drawPAGMap(DotGraph canvas, Map<? extends Node, ? extends Set<? extends Node>> map, String color) {
        map.forEach((n, elements) -> {
            elements.forEach(element -> {
                drawEdge(canvas, n, element, color);
            });
        });
    }

    private static void drawInvPAGMap(DotGraph canvas, Map<? extends Node, ? extends Set<? extends Node>> map, String color) {
        map.forEach((n, elements) -> {
            elements.forEach(element -> {
                drawEdge(canvas, element, n, color);
            });
        });
    }

    public static boolean isThrowable(Type type) {
        if (type instanceof RefType) {
            return PTAScene.v().getOrMakeFastHierarchy().canStoreType(type, RefType.v("java.lang.Throwable"));
        }
        return false;
    }

    public static boolean subtypeOfAbstractStringBuilder(Type t) {
        if (!(t instanceof RefType rt)) {
            return false;
        }
        String s = rt.toString();
        return (s.equals("java.lang.StringBuffer") || s.equals("java.lang.StringBuilder"));
    }

    public static boolean supportFinalize(AllocNode heap) {
        NumberedString sigFinalize = PTAScene.v().getSubSigNumberer().findOrAdd("void finalize()");
        Type type = heap.getType();
        if (type instanceof RefType refType && type != RefType.v("java.lang.Object")) {
            SootMethod finalizeMethod = VirtualCalls.v().resolveNonSpecial(refType, sigFinalize);
            if (finalizeMethod != null && finalizeMethod.toString().equals("<java.lang.Object: void finalize()>")) {
                return false;
            }
            return finalizeMethod != null;
        }
        return false;
    }

    public static Context plusplusOp(AllocNode heap) {
        ContextElement[] array;
        int s;
        if (heap instanceof ContextAllocNode csHeap) {
            ContextElements ctxElems = (ContextElements) csHeap.context();
            int ms = ctxElems.size();
            ContextElement[] oldArray = ctxElems.getElements();
            array = new ContextElement[ms + 1];
            array[0] = csHeap.base();
            System.arraycopy(oldArray, 0, array, 1, ms);
            s = ms + 1;
        } else {
            array = new ContextElement[1];
            array[0] = heap;
            s = 1;
        }
        return new ContextElements(array, s);
    }

    public static boolean isFakeMainMethod(SootMethod method) {
        String sig = "<FakeMain: void fakeMain()>";
        return method.getSignature().equals(sig);
    }

    /**
     * comma separated list of classes in which no matter what the length of k
     * for object sensitivity, we want to limit the depth of the object
     * sensitivity to 0. Also add subclasses of each
     */
    private static final String[] NO_CONTEXT = {"java.lang.Throwable", "java.lang.StringBuffer", "java.lang.StringBuilder"};
    private static Set<SootClass> ignoreList = null;

    /**
     * Install no context list for classes given plus all subclasses.
     */
    private static void initEmptyHeapContextTypes() {
        if (ignoreList == null) {
            ignoreList = new HashSet<>();
            Hierarchy hierarchy = new Hierarchy();
            for (String str : NO_CONTEXT) {
                SootClass clz = PTAScene.v().getSootClass(str);
                if (clz.isInterface()) {
                    for (SootClass child : hierarchy.getSubinterfacesOfIncluding(clz)) {
                        if (child != null) {
                            ignoreList.add(child);
                        }
                    }
                } else {
                    for (SootClass child : hierarchy.getSubclassesOfIncluding(clz)) {
                        if (child != null) {
                            ignoreList.add(child);
                        }
                    }
                }
            }
        }
    }

    public static boolean isOfPrimitiveBaseType(AllocNode heap) {
        if (heap.getType() instanceof ArrayType arrayType) {
            return arrayType.baseType instanceof PrimType;
        }
        return false;
    }

    public static boolean isPrimitiveArrayType(Type type) {
        if (type instanceof ArrayType arrayType) {
            return arrayType.getArrayElementType() instanceof PrimType;
        }
        return false;
    }

    /**
     * Limit heap context to 0 for these AllocNodes of types in ignoreList.
     * We set heaps of StringBuilder/StringBuffer/Throwable types to be context-insensitive just like that in Doop.
     * refers to library/string-constants.logic in doop.
     */
    public static boolean enforceEmptyContext(AllocNode probe) {
        // primitive array objects should be context-insenstive.
        if (probe.getType() instanceof ArrayType arrayType) {
            if (arrayType.getArrayElementType() instanceof PrimType) {
                return true;
            }
        }
        // shortcircuit below computation
        initEmptyHeapContextTypes();
        if (!ignoreList.isEmpty()) {
            // check if the type that is allocated should never has context
            // because it is on the ignore List
            SootClass allocated = getSootClass(probe.getType());
            // first check if on the no context list, that trumps all
            return ignoreList.contains(allocated);
        }
        return false;
    }

    /**
     * Write the jimple file for clz. ParentDir is the absolute path of parent
     * directory.
     */
    public static void writeJimple(String parentDir, SootClass clz) {

        File packageDirectory = new File(
                parentDir + File.separator + clz.getPackageName().replace(".", File.separator));

        try {
            packageDirectory.mkdirs();
            OutputStream streamOut = new FileOutputStream(
                    packageDirectory + File.separator + clz.getShortName() + ".jimple");
            PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
            Printer.v().printTo(clz, writerOut);
            writerOut.flush();
            writerOut.close();
            streamOut.close();

        } catch (Exception e) {
            logger.error("Error writing jimple to file {}", clz, e);
        }
    }

    public static void dumpJimple(String outputDir) {
        for (SootClass clz : PTAScene.v().getLibraryClasses()) {
            writeJimple(outputDir, clz);
        }
        for (SootClass clz : PTAScene.v().getApplicationClasses()) {
            writeJimple(outputDir, clz);
        }
    }

    /**
     * Given a file name with separators, convert them in to . so it is a legal
     * class name. modified: Ammonia: handle .* not only .class
     */
    public static String fromFileToClass(String name) {
        return name.substring(0, name.lastIndexOf('.')).replace(File.separatorChar, '.');
    }

    /**
     * Given a jarFile, return a list of the classes contained in the jarfile with .
     * replacing /.
     */
    public static List<String> getClassesFromJar(JarFile jarFile) {
        LinkedList<String> classes = new LinkedList<>();
        Enumeration<JarEntry> allEntries = jarFile.entries();

        while (allEntries.hasMoreElements()) {
            JarEntry entry = allEntries.nextElement();
            String name = entry.getName();
            if (!name.endsWith(".class")) {
                continue;
            }

            String clsName = name.substring(0, name.length() - 6).replace('/', '.');
            classes.add(clsName);
        }
        return classes;
    }

    public static String findMainFromMetaInfo(String appPath) {
        String mainClass = null;
        try {
            JarFile jar = new JarFile(appPath);
            Enumeration<JarEntry> allEntries = jar.entries();
            while (allEntries.hasMoreElements()) {
                JarEntry entry = allEntries.nextElement();
                String name = entry.getName();
                if (!name.endsWith(".MF")) {
                    continue;
                }
                String urlstring = "jar:file:" + appPath + "!/" + name;
                URL url = new URL(urlstring);
                Scanner scanner = new Scanner(url.openStream());
                while (scanner.hasNext()) {
                    String string = scanner.next();
                    if ("Main-Class:".equals(string) && scanner.hasNext()) {
                        mainClass = scanner.next();
                        break;
                    }
                }
                if (mainClass == null) {
                    System.out.println("cannot find meta info.");
                }
                scanner.close();
                jar.close();
                break;
            }
        } catch (IOException e) {
            System.out.println("cannot find meta info.");
        }
        return mainClass;
    }

    public static SootClass getSootClass(Type type) {
        SootClass allocated = null;
        if (type instanceof RefType) {
            allocated = ((RefType) type).getSootClass();
        } else if (type instanceof ArrayType && ((ArrayType) type).getArrayElementType() instanceof RefType) {
            allocated = ((RefType) ((ArrayType) type).getArrayElementType()).getSootClass();
        }
        return allocated;
    }

    private static final Map<SootMethod, Body> methodToBody = DataFactory.createMap();

    public static Body getMethodBody(SootMethod m) {
        Body body = methodToBody.get(m);
        if (body == null) {
            synchronized (PTAUtils.class) {
                if (body == null) {
                    if (m.isConcrete()) {
                        body = m.retrieveActiveBody();
                    } else {
                        body = new JimpleBody(m);
                    }
                    methodToBody.putIfAbsent(m, body);
                }
            }
        }
        return body;
    }

    public static boolean isEmptyArray(AllocNode heap) {
        Object var = heap.getNewExpr();
        if (var instanceof NewArrayExpr nae) {
            Value sizeVal = nae.getSize();
            if (sizeVal instanceof IntConstant size) {
                return size.value == 0;
            }
        }
        return false;
    }

    public static LocalVarNode paramToArg(PAG pag, Stmt invokeStmt, MethodPAG srcmpag, VarNode pi) {
        MethodNodeFactory srcnf = srcmpag.nodeFactory();
        InvokeExpr ie = invokeStmt.getInvokeExpr();
        Parm mPi = (Parm) pi.getVariable();
        LocalVarNode thisRef = (LocalVarNode) srcnf.caseThis();
        LocalVarNode receiver;
        if (ie instanceof InstanceInvokeExpr iie) {
            receiver = pag.findLocalVarNode(iie.getBase());
        } else {
            // static call
            receiver = thisRef;
        }
        if (mPi.isThis()) {
            return receiver;
        } else if (mPi.isReturn()) {
            if (invokeStmt instanceof AssignStmt assignStmt) {
                Value mR = assignStmt.getLeftOp();
                return (LocalVarNode) pag.findValNode(mR);
            } else {
                return null;
            }
        } else if (mPi.isThrowRet()) {
            return srcnf.makeInvokeStmtThrowVarNode(invokeStmt, srcmpag.getMethod());
        }
        // Normal formal parameters.
        Value arg = ie.getArg(mPi.getIndex());
        if (arg == null) {
            return null;
        } else {
            return pag.findLocalVarNode(arg);
        }
    }

    /*
     * We use this method to replace EntryPoints.v().clinitsOf() because it is infested with bugs.
     * */
    public static Set<SootMethod> clinitsOf(SootClass cl) {
        Set<SootMethod> ret = new HashSet<>();
        Set<SootClass> visit = new HashSet<>();
        Queue<SootClass> worklist = new UniqueQueue<>();
        SootClass curr = cl;
        while (curr != null) {
            worklist.add(curr);
            curr = curr.getSuperclassUnsafe();
        }
        while (!worklist.isEmpty()) {
            SootClass sc = worklist.poll();
            if (visit.add(sc)) {
                worklist.addAll(sc.getInterfaces());
            }
        }
        for (SootClass sc : visit) {
            final SootMethod initStart = sc.getMethodUnsafe(Scene.v().getSubSigNumberer().findOrAdd("void <clinit>()"));
            if (initStart != null) {
                ret.add(initStart);
            }
        }
        return ret;
    }
}