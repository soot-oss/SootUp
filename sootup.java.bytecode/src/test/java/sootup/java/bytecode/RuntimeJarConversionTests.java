package sootup.java.bytecode;

import categories.Java8Test;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.inputlocation.BytecodeClassLoadingOptions;
import sootup.java.bytecode.inputlocation.DefaultRTJarAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class RuntimeJarConversionTests {

  private static void execute(String methodSignature1) {
    AnalysisInputLocation inputLocation =
        new DefaultRTJarAnalysisInputLocation(
            SourceType.Library, BytecodeClassLoadingOptions.Default.getBodyInterceptors());

    final MethodSignature methodSignature =
        JavaIdentifierFactory.getInstance().parseMethodSignature(methodSignature1);

    JavaView view = new JavaView(Collections.singletonList(inputLocation));

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
  public void testBoundMethodHandle$FactoryGenerateConcreteBMHClass() {
    execute(
        "<java.lang.invoke.BoundMethodHandle$Factory: java.lang.Class generateConcreteBMHClass(java.lang.String)>");
  }

  @Test
  public void testFileDescriptorCloseAll() {
    execute("<java.io.FileDescriptor: void closeAll(java.io.Closeable)>");
  }
}
