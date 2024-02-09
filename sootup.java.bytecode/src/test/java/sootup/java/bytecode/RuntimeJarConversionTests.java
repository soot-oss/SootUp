package sootup.java.bytecode;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.java.bytecode.inputlocation.DefaultRTJarAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class RuntimeJarConversionTests {

  private static void convertInputLocation(AnalysisInputLocation inputLocation) {
    JavaView view = new JavaView(Collections.singletonList(inputLocation));
    long count =
        view.getClasses().stream()
            .flatMap(c -> c.getMethods().stream())
            .filter(SootMethod::isConcrete)
            .map(SootMethod::getBody)
            .count();
    Assert.assertTrue(count > 0);
  }

  @Test
  public void testJar() {
    AnalysisInputLocation inputLocation =
        new DefaultRTJarAnalysisInputLocation(SourceType.Library, Collections.emptyList());
    convertInputLocation(inputLocation);
  }

  @Test
  public void testJarWithDefaultInterceptors() {
    AnalysisInputLocation inputLocation = new DefaultRTJarAnalysisInputLocation(SourceType.Library);
    convertInputLocation(inputLocation);
  }

  /** helps debugging the conversion of a single method */
  private static void convertMethod(String methodSignature) {
    AnalysisInputLocation inputLocation = new DefaultRTJarAnalysisInputLocation(SourceType.Library);

    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    final SootMethod sootMethod =
        view.getMethod(view.getIdentifierFactory().parseMethodSignature(methodSignature)).get();
    sootMethod.getBody();
  }

  @Test
  public void testByteCodeClassTrap() {
    convertMethod("<java.awt.GraphicsEnvironment: java.awt.GraphicsEnvironment createGE()>");
  }

  @Test
  public void testTrapsicwUtility() {
    convertMethod(
        "<com.sun.org.apache.bcel.internal.classfile.Utility: java.lang.String signatureToString(java.lang.String,boolean)>");
  }

  @Test
  public void testTrapsicwUnresolvedPermission() {
    convertMethod(
        "<java.security.UnresolvedPermission: java.security.Permission resolve(java.security.Permission,java.security.cert.Certificate[])>");
  }

  @Test
  public void testTrapsicwStubFactoryFactoryStaticImpl() {
    // same exception range and type but different handler.. ->  duplicateCatchAllTrapRemover
    // adapted to handle java.lang.Exception as well
    convertMethod(
        "<com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryStaticImpl: javax.rmi.CORBA.Tie getTie(java.lang.Class)>");
  }

  @Test
  public void testTrapsicwUnixPrintJob$PrinterSpooler() {
    convertMethod(
        "<sun.print.UnixPrintJob$PrinterSpooler: void handleProcessFailure(java.lang.Process,java.lang.String[],int)>");
  }

  @Test
  public void testBoundMethodHandle$FactoryGenerateConcreteBMHClass() {
    convertMethod(
        "<java.lang.invoke.BoundMethodHandle$Factory: java.lang.Class generateConcreteBMHClass(java.lang.String)>");
  }

  @Test
  public void testFileDescriptorCloseAll() {
    convertMethod("<java.io.FileDescriptor: void closeAll(java.io.Closeable)>");
  }

  @Test
  public void testPlatformLogger_formatMessage() {
    convertMethod(
        "<sun.util.logging.PlatformLogger$DefaultLoggerProxy: java.lang.String formatMessage(java.lang.String,java.lang.Object[])>");
  }
}
