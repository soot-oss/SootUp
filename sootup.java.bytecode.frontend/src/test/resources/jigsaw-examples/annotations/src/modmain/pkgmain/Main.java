package pkgmain;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.ModuleLayer;
import java.lang.Module;

public class Main {
    public static void main(String[] args) throws IOException {
        Module modb = ModuleLayer.boot().findModule("modb").get();
        System.out.println("Annotations of module modb:");
        for (Annotation annotation: modb.getAnnotations()) {
        	System.out.println("- " + annotation);	
        }
        
        Module mod_annotations = ModuleLayer.boot().findModule("mod.annotations").get();
        System.out.println("\nAnnotations of module mod.annotations:");
        for (Annotation annotation: mod_annotations.getAnnotations()) {
        	System.out.println("- " + annotation);	
        }   
    }
}
