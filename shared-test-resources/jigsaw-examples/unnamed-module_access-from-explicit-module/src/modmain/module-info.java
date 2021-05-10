open module modmain { 	// allow reflective access, currently used in the example_jerry-mouse
    requires modb;
    // requires ALL-UNNAMED;   // this does not compile, hence we need to set --add-reads modmain=ALL-UNNAMED in both compile and run scripts!
}