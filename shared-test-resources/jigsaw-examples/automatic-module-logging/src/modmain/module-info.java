open module modmain { 	// allow reflective access, currently used in the example_jerry-mouse
    requires slf4j.api;		// note: must not be called slf4j-api (no '-' allowed in module names)
}