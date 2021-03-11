package pkgstarter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.module.*;
import java.lang.reflect.Method;
import java.lang.ModuleLayer;
import java.lang.Module;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class Starter {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: JerryMouse starter <baseDir>");
            System.exit(1);
        }

        String basedir = args[0];
        // $basedir/apps contain the startable applications
        Path appPath = Paths.get(basedir, "apps").toAbsolutePath().normalize();

        System.out.println("[JerryMouse] Scanning for apps in " + appPath.toString());

        ExecutorService executor = Executors.newSingleThreadExecutor(); // only one thread as otherwise logging output is cluttered and not sorted
        List<Future<?>> runningApps = new ArrayList<Future<?>>();
        try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(appPath, file -> Files.isDirectory(file))) {
                for (Path path : stream) {
                    String appName = path.normalize().getFileName().toString();

                    System.out.println("\n--------------------------------------------------------------------------------------------------------------------");
                    System.out.println("[JerryMouse|" +appName+ "] Initiating layer for application: " + appName);
                    System.out.println("[JerryMouse|" +appName+ "] Loading modules from "+ path.normalize().toString());

                    // Default: Root Module has the same name as the name of the app directory
                    String rootModuleName = path.getFileName().toString();
                    // Default: names for boot class ...
                    String bootClassName  = "Main";
                    // Default: ... and for boot method
                    String bootMethodName = "main";

                    File appJSONFile = new File(path.toFile(),"app.json");
                    System.out.println("[JerryMouse|" +appName+ "] Loading app description from " + appJSONFile.getCanonicalPath().toString());
                    try (InputStream in = new FileInputStream(appJSONFile)) {
                        JsonReader reader = Json.createReader(in);
                        JsonObject obj = reader.readObject();
                        rootModuleName = obj.getJsonString("rootModule").getString();
                        bootClassName  = obj.getJsonString("bootClass").getString();
                        bootMethodName = obj.getJsonString("bootMethod").getString();
                    } 
                    catch (FileNotFoundException fex) {
                        System.out.println("[JerryMouse|" +appName+ "] Error: " + appJSONFile + " not found for application " + appName + ". Using defaults.");
                        continue;
                    }

                    System.out.println("[JerryMouse|" +appName+ "] Root module: " + rootModuleName);
                    System.out.println("[JerryMouse|" +appName+ "] Boot class: "  + bootClassName);
                    System.out.println("[JerryMouse|" +appName+ "] Boot method: " + bootMethodName);

                    ModuleFinder finder = ModuleFinder.of(Paths.get(path.toString(), "mlib") );

                    Optional<ModuleReference> result = finder.find(rootModuleName);
                    if (! result.isPresent()) {
                        System.out.println("[JerryMouse|"+appName+"] Error: Root module " + rootModuleName + " not found.");
                    }
                    else {
                        try {
                            // Create Configuration based on the root module
                            Configuration cf = ModuleLayer.boot().configuration().resolve
                                    (ModuleFinder.of(), finder, Set.of(rootModuleName));

                            // Create new Jigsaw Layer with configuration and ClassLoader
                            ModuleLayer layer = ModuleLayer.boot().defineModulesWithOneLoader(cf, ClassLoader.getSystemClassLoader());

                            System.out.println("[JerryMouse|"+appName+"] Created layer containing the following modules:");
                            for (Module module : layer.modules()) {
                                System.out.println("         " + module.getName());
                            }

                            try {
                                // run the static method Boot.run() of the root module, done via reflection
                                //   (is executed in an ExecutorTask)
                                Class<?> bootClass  = layer.findLoader(rootModuleName).loadClass(bootClassName);

                                // addReads needed in order to be able to read the module
                                Starter.class.getModule().addReads(bootClass.getModule());
                                
                                // start the application
                                System.out.println("[JerryMouse|"+appName+"] Calling boot method (" + rootModuleName +"/"+ bootClassName 
                                        + "." + bootMethodName +") in background.");
                                String[] params = null;
                                Method   bootMethod = bootClass.getMethod(bootMethodName, String[].class);
                                Future<?> appTask = executor.submit(() -> {
                                    try {
                                        bootMethod.invoke(null, (Object) params);
                                    } 
                                    catch (Exception ex) {
                                        System.out.println("[JerryMouse|"+appName+"] Error: Caught exception:");
                                        ex.printStackTrace(System.out);
                                    }
                                });
                                runningApps.add(appTask);
                            } 
                            catch (ClassNotFoundException ex) {
                                System.out.println("[JerryMouse|"+appName+"] Error: Class " + bootClassName + " not found in default package for root module " + rootModuleName);
                                ex.printStackTrace(System.out);
                            } 
                            catch (NoSuchMethodException ex) {
                                System.out.println("[JerryMouse|"+appName+"] Error: Could not call method " + bootMethodName + " in class " + bootClassName + ".");
                                ex.printStackTrace(System.out);
                            }
                        }
                        catch (Exception ex) {
                            System.out.println("[JerryMouse|"+appName+"] Error: Caught exception:");
                            ex.printStackTrace(System.out);
                        }
                    } 
                }
                
                System.out.flush();
            }

            // wait for all tasks to complete
            for (Future<?> task: runningApps) {
                task.get();
                System.out.flush();
            }
        } 
        finally {
            System.out.println("\n[JerryMouse] All apps completed. Shutting down.");
            executor.shutdown();
        }
    }
}
