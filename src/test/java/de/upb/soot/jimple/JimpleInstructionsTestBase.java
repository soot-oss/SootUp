package de.upb.soot.jimple;

import de.upb.soot.core.SootClass;
import de.upb.soot.views.JavaView;

public abstract class JimpleInstructionsTestBase {

    public JavaView view;
    protected SootClass sootClass;
    String JimpleReferencePathPrefix = "src/test/java8/resources/reference-jimple/de.upb.soot.instructions.";

//    protected void build(){
//        Assume.assumeTrue(false);
//        Assert.fail("build() has to be overriden in a subclass");
//    }
//
//    @Test
//    public void compareGeneratedJimpleWithReferenceJimpleTest() throws IOException {
//
//        // generate filename from Testfilename
//        String name = this.getClass().getSimpleName();
//        name = name.substring(0, name.length()-4);
//
//        // build directory name
//        String directory = this.getClass().getName();
//        int endPos = directory.lastIndexOf('.');
//        int startPos = directory.lastIndexOf('.', endPos-1 ) +1;
//        directory = directory.substring( startPos, endPos);
//
//        // soot config
//        Project project = new Project();
//        view = new JavaView( project );
//
//        // build class structure
//        build();
//
//        StringWriter output = new StringWriter();
//        PrintWriter printWriter = new PrintWriter( output );
//
//        // print jimple to memory
//        new Printer().printTo( sootClass, printWriter );
//
//        printWriter.flush();
//        printWriter.close();
//
//        String referencJimple = FileUtils.readFileToString( new File(  JimpleReferencePathPrefix + directory + "." + name + ".jimple") , "UTF-8" );
//        Assert.assertEquals( referencJimple, output.toString());
//
//    }

}
