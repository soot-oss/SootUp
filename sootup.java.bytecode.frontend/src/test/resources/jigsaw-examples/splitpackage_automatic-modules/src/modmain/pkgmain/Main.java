package pkgmain;

import java.util.stream.Collectors;
import java.lang.module.ResolvedModule;

public class Main {
    public static void main(String[] args) throws Exception {
	    System.out.println("Hello World from pkgmain.Main");

	    System.out.println("Layer's list of modules:\n" 
	    		+ Main.class.getModule().getLayer().
	    		modules().stream().map(Module::getName).sorted().collect(Collectors.joining(", ")));
	    System.out.println("\nLayer's configuration with its list of resolved modules:\n" 
	    		+ Main.class.getModule().getLayer().configuration().
	    		modules().stream().map(ResolvedModule::name).sorted().collect(Collectors.joining(", ")));
    }
}
