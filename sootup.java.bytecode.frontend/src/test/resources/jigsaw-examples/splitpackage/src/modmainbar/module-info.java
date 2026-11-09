open module modmainbar { 	// allow reflective access, currently used in the example_jerry-mouse
    // does not compile, if both are required
    requires modsplitbar1;

    // we hence do not require modsplitbar2:
    //    requires modsplitbar2;
    // but we use --add-modules in our run script / launch configuration -> produces a java.lang.LayerInstantiationException during startup
}
