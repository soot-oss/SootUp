package sootup.tests;

import org.junit.jupiter.api.Test;
import sootup.core.air.AIRConverter;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.PrimitiveType;
import sootup.core.types.VoidType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AIRTest {

    protected JavaIdentifierFactory identifierFactory;
    protected JavaClassType mainClassSignature;
    protected MethodSignature mainMethodSignature;
    private String algorithmName;
    private JavaView view;

    private JavaView createViewForClassPath(String classPath) {
        List<AnalysisInputLocation> inputLocations = new ArrayList<>();
        inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
        inputLocations.add(new JavaClassPathAnalysisInputLocation(classPath, SourceType.Application, Collections.emptyList()));
        return new JavaView(inputLocations);
    }

    @Test
    public void createJimple(){

        String classPath = "src/test/resources/" + "air/programs/binary";

        view = createViewForClassPath(classPath);
        identifierFactory = view.getIdentifierFactory();

        mainClassSignature = identifierFactory.getClassType("SimpleConstant");
        mainMethodSignature =
                identifierFactory.getMethodSignature(
                        mainClassSignature, identifierFactory.getMethodSubSignature(
                                "main", PrimitiveType.IntType.getInstance(), Collections.singletonList(identifierFactory.getType("java.lang.String[]"))));
        SootClass sc = view.getClass(mainClassSignature).orElse(null);
        assertNotNull(sc);
        SootMethod m = sc.getMethod(mainMethodSignature.getSubSignature()).orElse(null);
        assert m != null;
        m.getBody().getStmts().forEach(System.out::println);
        System.out.println("***********AIR Statements************");
        new AIRConverter().convert(m.getBody()).forEach(System.out::println);
    }
}
