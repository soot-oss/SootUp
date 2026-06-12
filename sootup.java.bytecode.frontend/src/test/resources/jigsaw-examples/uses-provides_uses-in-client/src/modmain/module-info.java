open module modmain { 	// allow reflective access, currently used in the example_jerry-mouse
    requires modservicedefinition;
    uses myservice.IService;
}