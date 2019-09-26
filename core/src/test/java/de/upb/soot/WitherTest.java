package de.upb.soot;

import static org.junit.Assert.fail;

import org.junit.Ignore;

public class WitherTest {

  @Ignore
  public void dummyTest() {
    // TODO: please rewrite without the need of javasourcecodefrontend as source here :(
    fail(" rewrite test or put it in integration test module");
  }

  /*
   private WalaClassLoader loader;
   private DefaultIdentifierFactory identifierFactory;
   private JavaClassType declareClassSig;

   @Before
   public void loadClassesWithWala() {
     String srcDir = "src/test/resources/selected-java-target/";
     loader = new WalaClassLoader(srcDir, null);
     identifierFactory = DefaultIdentifierFactory.getInstance();
     declareClassSig = identifierFactory.getClassType("BinaryOperations");
   }

   @Test
   public void testWithers() {
     Optional<ClassSource> classSource = loader.getClassSource(declareClassSig);
     assertTrue(classSource.isPresent());
     SootClass sootClass = new SootClass(classSource.get(), SourceType.Application);

     Optional<SootMethod> m =
         sootClass.getMethod(
             identifierFactory.getMethodSignature(
                 "addByte", declareClassSig, "byte", Arrays.asList("byte", "byte")));
     assertTrue(m.isPresent());
     SootMethod method = m.get();

     Body body = method.getBody();
     assertNotNull(body);

     // Let's change a name of a variable deep down in the body of a method of a class
     SootClass newSootClass =
         sootClass.withOverridingClassSource(
             overridingClassSource -> {
               SootMethod newMethod =
                   method.withOverridingMethodSource(
                       methodSource -> {
                         JIdentityStmt stmt = (JIdentityStmt) body.getStmts().get(0);
                         Local local = (Local) stmt.getLeftOp();
                         Local newLocal = local.withName("newName");
                         Stmt newStmt = stmt.withLocal(newLocal);

                         return methodSource.withBodyStmts(newStmts -> newStmts.set(0, newStmt));
                       });
               return overridingClassSource.withReplacedMethod(method, newMethod);
             });

     Optional<SootMethod> newM = newSootClass.getMethod(method.getSignature());
     assertTrue(newM.isPresent());
     Body newBody = newM.get().getBody();
     assertNotNull(newBody);
     JIdentityStmt newJIdentityStmt = (JIdentityStmt) newBody.getStmts().get(0);
     assertEquals("newName", ((Local) newJIdentityStmt.getLeftOp()).getName());

     assertNotEquals(
         "newName", ((Local) ((JIdentityStmt) body.getStmts().get(0)).getLeftOp()).getName());
   }

  */
}
