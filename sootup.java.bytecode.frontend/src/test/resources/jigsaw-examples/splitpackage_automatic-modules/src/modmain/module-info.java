open module modmain { 	// allow reflective access, currently used in the example_jerry-mouse
     requires modauto1; // modmain requires modauto1. Hence modauto1 requires all other automatic modules on the module path automatically.
}
