package de.upb.swt.soot.java.bytecode;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.bytecode.inputlocation.BytecodeClassLoadingOptions;
import de.upb.swt.soot.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import java.nio.file.Paths;
import org.junit.Test;

public class RuntimeJarConversionTests {

  private static void execute(String methodSignature1) {
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new PathBasedAnalysisInputLocation(
            Paths.get(System.getProperty("java.home") + "/lib/rt.jar"), null);
    JavaProject project =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(inputLocation).build();

    final MethodSignature methodSignature =
        JavaIdentifierFactory.getInstance().parseMethodSignature(methodSignature1);

    JavaView view =
        project.createOnDemandView(analysisInputLocation -> BytecodeClassLoadingOptions.Default);

    final SootMethod sootMethod = view.getMethod(methodSignature).get();
    sootMethod.getBody();
  }

  @Test
  public void testByteCodeClassTrap() {
    execute("<java.awt.GraphicsEnvironment: java.awt.GraphicsEnvironment createGE()>");
  }

  @Test
  public void testTrapsicwUtility() {
    execute(
        "<com.sun.org.apache.bcel.internal.classfile.Utility: java.lang.String signatureToString(java.lang.String,boolean)>");
  }

  @Test
  public void testTrapsicwUnresolvedPermission() {
    execute(
        "<java.security.UnresolvedPermission: java.security.Permission resolve(java.security.Permission,java.security.cert.Certificate[])>");
  }

  @Test
  public void testTrapsicwStubFactoryFactoryStaticImpl() {
    // same exception range and type but different handler.. ->  duplicateCatchAllTrapRemover
    // adapted to handle java.lang.Exception as well
    execute(
        "<com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryStaticImpl: javax.rmi.CORBA.Tie getTie(java.lang.Class)>");
  }

  @Test
  public void testTrapsicwUnixPrintJob$PrinterSpooler() {
    execute(
        "<sun.print.UnixPrintJob$PrinterSpooler: void handleProcessFailure(java.lang.Process,java.lang.String[],int)>");
  }

  @Test
  public void testReplaceStmtm() {
    execute(
        "<java.lang.invoke.BoundMethodHandle$Factory: java.lang.Class generateConcreteBMHClass(java.lang.String)>");
  }
}
