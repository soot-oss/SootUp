package sootup.java.bytecode;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.util.DotExporter;
import sootup.core.util.Utils;
import sootup.java.bytecode.inputlocation.DefaultRTJarAnalysisInputLocation;
import sootup.java.bytecode.interceptors.BytecodeBodyInterceptors;
import sootup.java.bytecode.interceptors.CopyPropagator;
import sootup.java.bytecode.interceptors.DeadAssignmentEliminator;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class RuntimeJarConversionTests {
  private static boolean debug = false;

  private static void convertInputLocation(AnalysisInputLocation inputLocation) {
    JavaView view = new JavaView(Collections.singletonList(inputLocation));
    long classesCount = view.getClasses().stream().count();
    if (debug) {
      System.out.println("classes: " + classesCount);
    }
    int[] failedConversions = {0};
    long[] progress = {0};

    long count =
        view.getClasses().stream()
            .peek(
                c -> {
                  if (!debug) {
                    return;
                  }
                  System.out.println(
                      "converted classes: "
                          + progress[0]
                          + "  failed: "
                          + failedConversions[0]
                          + " - progress "
                          + ((double) progress[0]++ / classesCount));
                })
            .flatMap(c -> c.getMethods().stream())
            .filter(SootMethod::isConcrete)
            .peek(
                javaSootMethod -> {
                  try {
                    javaSootMethod.getBody();
                  } catch (Exception e) {
                    e.printStackTrace();
                    failedConversions[0]++;
                  }
                })
            .count();
    Assert.assertTrue(count > 0);
    Assert.assertEquals(0, failedConversions[0]);
  }

  // @Test
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

    BiFunction<BodyInterceptor, Body.BodyBuilder, Boolean> step =
        (interceptor, builder) -> {
          if (interceptor.getClass() != CopyPropagator.class
              && interceptor.getClass() != DeadAssignmentEliminator.class) {
            return false;
          }
          System.out.println(DotExporter.createUrlToWebeditor(builder.getStmtGraph()));

          return true;
        };

    List<BodyInterceptor> bodyInterceptors =
        Utils.wrapEachBodyInterceptorWith(
            BytecodeBodyInterceptors.Default.getBodyInterceptors(), step);
    AnalysisInputLocation inputLocation =
        new DefaultRTJarAnalysisInputLocation(SourceType.Library, bodyInterceptors);

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

  @Test
  public void testSwingUtilities2() {
    // ConditionalBranchfolder cast of evaluated result
    convertMethod("<sun.swing.SwingUtilities2: boolean canAccessSystemClipboard()>");
  }

  @Test
  public void testZoneInfoFile_getZoneInfo() {
    // TypeAssigner fails
    convertMethod(
        "<sun.util.calendar.ZoneInfoFile: sun.util.calendar.ZoneInfo getZoneInfo(java.lang.String,long[],int[],long[],int[],sun.util.calendar.ZoneInfoFile$ZoneOffsetTransitionRule[])>");
  }

  @Test
  public void testSunSecPasswd() {
    // ConditionalBranchFolder fails -
    convertMethod("<sun.security.util.Password: char[] readPassword(java.io.InputStream,boolean)>");
  }

  @Test
  public void testXRUtils() {
    //   CastAndReturnInliner fails
    convertMethod("<sun.java2d.xr.XRUtils: short clampToShort(int)>");
  }

  @Test
  public void testImageIcon() {
    convertMethod("<javax.swing.ImageIcon: java.awt.MediaTracker getTracker()>");
  }

  @Test
  public void testSAX2DTM2() {
    // LocalSplitter
    convertMethod(
        "<com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2: void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes)>");
  }

  @Test
  public void testInclude_parseContents() {
    // LocalSplitter
    convertMethod(
        "<com.sun.org.apache.xalan.internal.xsltc.compiler.Include: void parseContents(com.sun.org.apache.xalan.internal.xsltc.compiler.Parser)>");
  }

  @Test
  public void testReferencePropertyInfoImpl() {
    // LocalSplitter - assignStmt not in graph
    convertMethod(
        "<com.sun.xml.internal.bind.v2.model.impl.ReferencePropertyInfoImpl: void calcTypes(boolean)>");
  }

  @Test
  public void testREUtil() {
    // LocalSplitter
    convertMethod(
        "<com.sun.org.apache.xerces.internal.impl.xpath.regex.REUtil: void main(java.lang.String[])>");
  }

  @Test
  public void testResolverCatalog() {
    // LocalSplitter
    convertMethod(
        "<com.sun.org.apache.xml.internal.resolver.Catalog: java.lang.String resolveLocalSystem(java.lang.String)>");
  }

  @Test
  public void testWrapperBeanGenerator() {
    // LocalSplitter
    convertMethod(
        "<com.sun.xml.internal.ws.model.WrapperBeanGenerator: byte[] createBeanImage(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Collection)>");
  }

  @Test
  public void testString() {
    // CopyPropagator
    convertMethod("<java.lang.String: void <init>()>");
  }

  @Test
  public void testSunToolkit() {
    // fails at DeadAssgnment.. after CopyPropagator inlined Stmts -> StmtGraphIterator fails as the
    // order changed -> traps are not build correctly.. fix iterator!
    convertMethod("<sun.awt.SunToolkit: boolean imageExists(java.net.URL)>");
  }

  @Test
  public void testMonthDay() {
    //   ConditionalBranchFolder
    convertMethod("<java.time.MonthDay: boolean isValidYear(int)>");
  }

  @Test
  public void testWSDLGenerator() {
    //   ConditionalBranchFolder
    convertMethod(
        "<com.sun.xml.internal.ws.wsdl.writer.WSDLGenerator: void generateBindingOperation(com.sun.xml.internal.ws.model.JavaMethodImpl,com.sun.xml.internal.ws.wsdl.writer.document.Binding)>");
  }
}
