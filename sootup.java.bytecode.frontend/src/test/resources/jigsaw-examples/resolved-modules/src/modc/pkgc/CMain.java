package pkgc;

import java.lang.ModuleLayer;
import java.util.ArrayList;

public class CMain {
    public static void main(String[] args) {
        System.out.println("Now we have these boot layer modules: ");

        ArrayList<String> modNames = new ArrayList<>();
        ModuleLayer.boot()
          .modules()
          .stream()
          .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
          .forEach(mod -> modNames.add(mod.getName()));

        System.out.println(modNames);
    }
}
