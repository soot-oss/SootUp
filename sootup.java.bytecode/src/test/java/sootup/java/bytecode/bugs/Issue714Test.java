package sootup.java.bytecode.bugs;

import categories.Java9Test;
import java.util.Collections;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.graph.StmtGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.DotExporter;
import sootup.java.bytecode.inputlocation.BytecodeClassLoadingOptions;
import sootup.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

@Category(Java9Test.class)
public class Issue714Test {

  @Test
  public void slow_localSplitter() {
    // "jdk.internal.jimage.ImageStringsReader :: mutf8FromString will cause LocalSplitter not halt
    // / comsume unreasonable time"
    JavaProject applicationProject =
        JavaProject.builder(new JavaLanguage(8))
            .enableModules()
            .addInputLocation(new JrtFileSystemAnalysisInputLocation())
            .build();

    JavaView view = applicationProject.createMutableView();
    view.configBodyInterceptors(analysisInputLocation -> BytecodeClassLoadingOptions.Default);

    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .parseMethodSignature(
                "<jdk.internal.jimage.ImageStringsReader: int mutf8FromString(java.lang.String)>");
    final Optional<JavaSootClass> classOpt = view.getClass(methodSignature.getDeclClassType());
    Assert.assertTrue(classOpt.isPresent());

    final Optional<? extends SootMethod> methodOpt = view.getMethod(methodSignature);
    Assert.assertTrue(methodOpt.isPresent());

    final StmtGraph<?> stmtGraph = methodOpt.get().getBody().getStmtGraph();

    System.out.println(DotExporter.createUrlToWebeditor(stmtGraph));
    System.out.println(stmtGraph);
  }

  @Test
  public void slow_TypeAssigner() {
    //     java.lang.Character$UnicodeScript :: <clinit> will cause TypeAssigner not halt / comsume
    // unreasonable time. It seems that the real problem occurs in the LocalNameStandardizer.

    JavaProject applicationProject =
        JavaProject.builder(new JavaLanguage(11))
            .enableModules()
            .addInputLocation(new JrtFileSystemAnalysisInputLocation())
            .build();

    JavaView view = applicationProject.createMutableView();
    view.configBodyInterceptors(analysisInputLocation -> BytecodeClassLoadingOptions.Default);

    // https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Character.UnicodeScript.html
    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                "java.base/java.lang.Character$UnicodeScript",
                "<clinit>",
                "void",
                Collections.singletonList(""));
    final Optional<JavaSootClass> classOpt = view.getClass(methodSignature.getDeclClassType());
    Assert.assertTrue(classOpt.isPresent());

    final Optional<? extends SootMethod> methodOpt = view.getMethod(methodSignature);
    Assert.assertTrue(methodOpt.isPresent());

    final StmtGraph<?> stmtGraph = methodOpt.get().getBody().getStmtGraph();

    System.out.println(DotExporter.createUrlToWebeditor(stmtGraph));
    System.out.println(stmtGraph);
  }

  @Test
  public void bloated_AsmMethodSource_worklist() {
    //    sun.jvm.hotspot.ui.classbrowser.HTMLGenerator :: genHTMLListForFields, with desc
    // (Lsun/jvm/hotspot/oops/InstanceKlass;)Ljava/lang/String;, will cause AsmMethodSource ::
    // convert not halt / comsume unreasonable time. It worth noting that the worklist will be full
    // of elements, even through the length of instructions list is just 172. Please refer to the
    // attached screenshot for more details.

    JavaProject applicationProject =
        JavaProject.builder(new JavaLanguage(11))
            .enableModules()
            .addInputLocation(new JrtFileSystemAnalysisInputLocation())
            .build();

    JavaView view = applicationProject.createMutableView();
    view.configBodyInterceptors(analysisInputLocation -> BytecodeClassLoadingOptions.Default);

    // https://code.yawk.at/java/11/jdk.hotspot.agent/sun/jvm/hotspot/ui/classbrowser/HTMLGenerator.java#sun.jvm.hotspot.ui.classbrowser.HTMLGenerator%23genHTMLListForFields(sun.jvm.hotspot.oops.InstanceKlass)
    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                "jdk.hotspot.agent/sun.jvm.hotspot.ui.classbrowser.HTMLGenerator",
                "genHTMLListForFields",
                "java.lang.String",
                Collections.singletonList("jdk.hotspot.agent/sun.jvm.hotspot.oops.InstanceKlass"));
    final Optional<JavaSootClass> classOpt = view.getClass(methodSignature.getDeclClassType());
    Assert.assertTrue(classOpt.isPresent());

    final Optional<? extends SootMethod> methodOpt = view.getMethod(methodSignature);
    Assert.assertTrue(methodOpt.isPresent());

    final StmtGraph<?> stmtGraph = methodOpt.get().getBody().getStmtGraph();

    System.out.println(DotExporter.createUrlToWebeditor(stmtGraph));
    System.out.println(stmtGraph);
  }
}
