package de.upb.soot.frontends.java;

import de.upb.soot.core.SootClass;

import java.util.List;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.options.Options;

/**
 * This example demonstrate how we use WALA java source code fronend-end to generate jimple in old soot and perform analysis.
 * 
 * @author Linghui Luo
 *
 */
public class Example {

  public static void main(String... args) {
    // set up soot options
    G.v().reset();
    Options.v().set_whole_program(true);
    Options.v().setPhaseOption("cg.spark", "on");

    // load basic classes from soot
    Scene.v().loadBasicClasses();

    String sourceDirPath = "XXX";
    String exclusionFilePath = "";
    // Use WALA java front-end to load classes and convert to soot classes
    WalaClassLoader loader = new WalaClassLoader(sourceDirPath, exclusionFilePath);
    List<SootClass> sootClasses = loader.getSootClasses();

    // Convert classes in new jimple to old one
    JimpleConverter jimpleConverter=new JimpleConverter();
    for(SootClass sootClass: sootClasses) {
      soot.SootClass klass = jimpleConverter.convertSootClass(sootClass);
      // add each class to Scene
      Scene.v().addClass(klass);
    }

    // TODO. implement your analysis in transform, source code location info are stored in tag
    Transform t = null;

    // build call graph and run analysis
    PackManager.v().getPack("cg").apply();
    // add your analysis to wjtp pack
    PackManager.v().getPack("wjtp").add(t);
    PackManager.v().getPack("wjtp").apply();
  }

}
