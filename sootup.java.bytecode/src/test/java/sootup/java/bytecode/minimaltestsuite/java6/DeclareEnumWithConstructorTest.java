package sootup.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class DeclareEnumWithConstructorTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getInitMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "<init>", "void", Collections.emptyList());
  }

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "getValue", "int", Collections.emptyList());
  }

  public MethodSignature getMainMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(),
        "main",
        "void",
        Collections.singletonList("java.lang.String[]"));
  }

  public MethodSignature getEnumConstructorSignature() {
    return identifierFactory.getMethodSignature(
        identifierFactory.getClassType("DeclareEnumWithConstructor$Number"),
        "<clinit>",
        "void",
        Collections.emptyList());
  }

  public MethodSignature getEnumGetValueSignature() {
    return identifierFactory.getMethodSignature(
        identifierFactory.getClassType("DeclareEnumWithConstructor$Number"),
        "getValue",
        "int",
        Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod sootMethod = loadMethod(getInitMethodSignature());
    assertJimpleStmts(sootMethod, expectedBodyStmts());

    sootMethod = loadMethod(getMainMethodSignature());
    assertJimpleStmts(sootMethod, expectedMainBodyStmts());

    sootMethod = loadMethod(getEnumConstructorSignature());
    assertJimpleStmts(sootMethod, expectedEnumConstructorStmts());

    sootMethod = loadMethod(getEnumGetValueSignature());
    assertJimpleStmts(sootMethod, expectedGetValueStmts());
    JavaSootClass sootClass =
        loadClass(
            JavaIdentifierFactory.getInstance()
                .getClassType(getDeclaredClassSignature().getFullyQualifiedName() + "$Number"));
    assertTrue(sootClass.isEnum());

    final Set<? extends JavaSootMethod> methods = sootClass.getMethods();
    assertTrue(methods.stream().anyMatch(m -> m.getSignature().getName().equals("getValue")));
  }

  /**
   *
   *
   * <pre>    public void declareEnum(){
   * for(Type type:Type.values()){
   * System.out.println(type);
   * }
   * }
   * }
   * </pre>
   */
  /**
   *
   *
   * <pre>        private int getValue() {
   * return value;
   * }
   * </pre>
   */

  /**
   *
   *
   * <pre>
   * public void declareEnum(){
   * for(Type type:Type.values()){
   * System.out.println(type);
   * }
   * }
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: DeclareEnumWithConstructor",
            "specialinvoke l0.<java.lang.Object: void <init>()>()",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public static void main(String[] args) {
   *         Number number = Number.ONE;
   *         System.out.println(number.getValue());
   *     }
   * </pre>
   */
  public List<String> expectedMainBodyStmts() {
    return Stream.of(
            "l0 := @parameter0: java.lang.String[]",
            "l1 = <DeclareEnumWithConstructor$Number: DeclareEnumWithConstructor$Number ONE>",
            "$stack2 = <java.lang.System: java.io.PrintStream out>",
            "$stack3 = staticinvoke <DeclareEnumWithConstructor$Number: int access$000(DeclareEnumWithConstructor$Number)>(l1)",
            "virtualinvoke $stack2.<java.io.PrintStream: void println(int)>($stack3)",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public enum Number{
   *         ZERO(0),
   *         ONE(1),
   *         TWO(2),
   *         THREE(3);
   *         private int value;
   *         Number(int value){
   *             this.value=value;
   *         }
   *  }
   * </pre>
   */
  public List<String> expectedEnumConstructorStmts() {
    return Stream.of(
            "$stack0 = new DeclareEnumWithConstructor$Number",
            "specialinvoke $stack0.<DeclareEnumWithConstructor$Number: void <init>(java.lang.String,int,int)>(\"ZERO\", 0, 0)",
            "<DeclareEnumWithConstructor$Number: DeclareEnumWithConstructor$Number ZERO> = $stack0",
            "$stack1 = new DeclareEnumWithConstructor$Number",
            "specialinvoke $stack1.<DeclareEnumWithConstructor$Number: void <init>(java.lang.String,int,int)>(\"ONE\", 1, 1)",
            "<DeclareEnumWithConstructor$Number: DeclareEnumWithConstructor$Number ONE> = $stack1",
            "$stack2 = new DeclareEnumWithConstructor$Number",
            "specialinvoke $stack2.<DeclareEnumWithConstructor$Number: void <init>(java.lang.String,int,int)>(\"TWO\", 2, 2)",
            "<DeclareEnumWithConstructor$Number: DeclareEnumWithConstructor$Number TWO> = $stack2",
            "$stack3 = new DeclareEnumWithConstructor$Number",
            "specialinvoke $stack3.<DeclareEnumWithConstructor$Number: void <init>(java.lang.String,int,int)>(\"THREE\", 3, 3)",
            "<DeclareEnumWithConstructor$Number: DeclareEnumWithConstructor$Number THREE> = $stack3",
            "$stack4 = newarray (DeclareEnumWithConstructor$Number)[4]",
            "$stack5 = <DeclareEnumWithConstructor$Number: DeclareEnumWithConstructor$Number ZERO>",
            "$stack4[0] = $stack5",
            "$stack6 = <DeclareEnumWithConstructor$Number: DeclareEnumWithConstructor$Number ONE>",
            "$stack4[1] = $stack6",
            "$stack7 = <DeclareEnumWithConstructor$Number: DeclareEnumWithConstructor$Number TWO>",
            "$stack4[2] = $stack7",
            "$stack8 = <DeclareEnumWithConstructor$Number: DeclareEnumWithConstructor$Number THREE>",
            "$stack4[3] = $stack8",
            "<DeclareEnumWithConstructor$Number: DeclareEnumWithConstructor$Number[] $VALUES> = $stack4",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>public enum Number{
   *         ZERO(0),
   *         ONE(1),
   *         TWO(2),
   *         THREE(3);
   *         private int value;
   *
   *     private int getValue() {
   *             return value;
   *         }
   *         }
   * </pre>
   */
  public List<String> expectedGetValueStmts() {
    return Stream.of(
            "l0 := @this: DeclareEnumWithConstructor$Number",
            "$stack1 = l0.<DeclareEnumWithConstructor$Number: int value>",
            "return $stack1")
        .collect(Collectors.toList());
  }
}
