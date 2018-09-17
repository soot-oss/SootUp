package de.upb.soot.frontends.java;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.ir.ssa.AstIRFactory.AstIR;
import com.ibm.wala.cast.java.analysis.typeInference.AstJavaTypeInference;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cfg.AbstractCFG;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.SourceDirectoryTreeModule;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.io.CommandLine;
import com.ibm.wala.util.warnings.Warnings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.JarFile;

public class WalaIRtoJimple {

  public static void main(String[] args)
      throws ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException, IOException {
    long start = System.currentTimeMillis();
    Properties p = CommandLine.parse(args);
    String sourceDir = "E:\\test\\wala";
    String applicationClass = "Lde/upb/soot/controlStatements/ControlStatements";
    PrintWriter out = new PrintWriter(sourceDir + File.separator + "out.txt");
    AnalysisScope scope = new JavaSourceAnalysisScope();
    // add standard libraries to scope
    String[] stdlibs = WalaProperties.getJ2SEJarFiles();
    for (String stdlib : stdlibs) {
      scope.addToScope(ClassLoaderReference.Primordial, new JarFile(stdlib));
    }
    // add the source directory
    scope.addToScope(JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(sourceDir)));
    FileOfClasses classes = new FileOfClasses(
        new FileInputStream(new File("E:\\Git\\WALA\\com.ibm.wala.core.tests\\dat\\Java60RegressionExclusions.txt")));
    scope.setExclusions(classes);
    // build the class hierarchy
    IClassHierarchy cha = ClassHierarchyFactory.make(scope, new ECJClassLoaderFactory(scope.getExclusions()));
    Warnings.clear();
    // AnalysisOptions options = new AnalysisOptions();
    // Iterable<Entrypoint> entrypoints
    // = Util.makeMainEntrypoints(JavaSourceAnalysisScope.SOURCE, cha, new String[] { mainClass });
    //
    // options.setEntrypoints(entrypoints);
    // you can dial down reflection handling if you like
    // options.setReflectionOptions(ReflectionOptions.NONE);
    IAnalysisCacheView cache = new AnalysisCacheImpl(AstIRFactory.makeDefaultFactory());
    Iterator<IClass> it = cha.iterator();
    while (it.hasNext()) {
      IClass klass = it.next();
      if (klass.getName().toString().equals(applicationClass)) {
        JavaClass clss = (JavaClass) klass;
        for (IMethod method : clss.getAllMethods()) {
          if (method instanceof AstMethod) {
            AstIR ir = (AstIR) cache.getIR(method);
            AstJavaTypeInference typeInf = new AstJavaTypeInference(ir, true);
            typeInf.solve();
            // print instructions of each methods
            StringBuffer sb = new StringBuffer();
            System.out.println(method.getSignature());
            sb.append(method.getSignature() + "{\n");
            AbstractCFG<?, ?> cfg = ((AstMethod) method).cfg();
            SSAInstruction[] insts = (SSAInstruction[]) cfg.getInstructions();
            SymbolTable symbolTable = new SymbolTable(method.getNumberOfParameters());
            for (SSAInstruction inst : insts) {
              int index = inst.iindex;
              SSAInstruction irInst = ir.getInstructions()[index];
              if (irInst != null) {// get type
                for (int i = 0; i < irInst.getNumberOfUses(); i++) {
                  System.out.println(typeInf.getType(irInst.getUse(i)));
                }
              }
              System.out.println();
              sb.append("\t" + inst.toString(symbolTable) + "\n");
              sb.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
              sb.append(inst.getClass().getName() + "\n");
            }
            sb.append("}\n");
            out.println(sb.toString());
          }
        }
      }

    }
    out.close();
    // CallGraphBuilder builder = new ZeroCFABuilderFactory().make(options, cache, cha, scope);
    // CallGraphBuilder<?> builder = new ZeroOneContainerCFABuilderFactory().make(options, cache, cha, scope);
    // System.out.println("building call graph...");
    // CallGraph cg = builder.makeCallGraph(options, null);
    // long end = System.currentTimeMillis();
    // System.out.println("done");
    // System.out.println("took " + (end - start) + "ms");
    // System.out.println(CallGraphStats.getStats(cg));
  }

}
