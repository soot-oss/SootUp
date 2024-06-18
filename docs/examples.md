# SootUp Example projects
Some examples that use SootUp to get insights about a Java program.

- Basic setup [Example](https://github.com/soot-oss/SootUp-Examples/blob/main/BasicSetupExample/src/main/java/sootup/examples/BasicSetup.java)
- Configure a BodyInterceptor [Example](https://github.com/soot-oss/SootUp-Examples/blob/main/BodyInterceptorExample/src/main/java/sootup/examples/BodyInterceptor.java)
- CallGraph [Example](https://github.com/soot-oss/SootUp-Examples/blob/main/CallgraphExample/src/main/java/sootup/examples/CallgraphExample.java)
- Class Hierarchy Algoritm [Example](https://github.com/soot-oss/SootUp-Examples/blob/main/ClassHierarchyExample/src/main/java/sootup/examples/ClassHierarchy.java)
- Replace a SootMethod of a SootClass [Example](https://github.com/soot-oss/SootUp-Examples/blob/main/MutatingSootClassExample/src/main/java/sootup/examples/MutatingSootClass.java)


!!! info "Download"
    The Examples can be cloned or downloaded from our [Example Repository](https://github.com/soot-oss/SootUp-Examples.git).



<!--
We have included all the five projects in 5 different branches under SootUp-Examples with detailed explanation about the project.

### BasicSetupExample
1. package sootup.examples; - defines the package name for the Java class.
2. import statement - defines various classes and interfaces from different packages that the program uses.
3. public class BasicSetup - declares a public class named 'BasicSetup' which is the main class for this program.
4. Then we have created a main method which is the entry point of the progrram
5. Path pathToBinary object pointing to a directory that contains the binary files ie class files to be analyzed and Paths.get is a static method that converts string path to a 'Path' object.
6. AnalysisInputLocation object specifying where SootUp should look for classes to analyze.
7. View object is created for the project allowing the retrieal of classes from the specified input location. JavaView is specific implementation of View tailed for Java projects.
8. The ClassType object is created for the class name 'HelloWorld'. This object represents the type of class to be analyzed.
9. A MethodSignature object is created for the main method of the HelloWorld class. This signature specifies the method's return type (void) and its parameter types (a single parameter of type String[]).
10. The if statment checks for the presences of the class 'HelloWorld'. If not it prints "Class not ffound!" and exits the program.
11. Then it retrieves the SootClass object representing the HelloWorld class, assuming it is present.
12. view.getMethod(methodSignature); - Attempts to retrieve the specified method from the project.
13. The if statment after this, checks if the main method is present in the HelloWorld class. If not, it prints "Method not found!" and exits.
14. Then the next statment retrieves the SootMethod object for the main method and prints its body, which is in Jimple, a simplified version of Java bytecode used by Soot for analysis and transformation.
15. Then the next if condition checks if the method containts a specific statement called 'Hello World!'.

### BodyInterceptor
1. package sootup.examples; - defines the package name for the Java class.
2. import statement - defines various classes and interfaces from different packages that the program uses.
3. public class BodyInterceptor - declares a public class named "BodyInterceptor".
4. Then we have created a main method in which is the entry point for the code.
5. Then we have created an AnalysisInputLocation pointing to a directory with class files to be loaded. It specifies that the DeadAssignmentEliminator interceptor should be applied to these classes.
6. Then created a View that initializes a JavaView with the specified inputLocation, allowing interaction with the classes for analysis.
7. Then have created a ClassType and MethodSignature which is used for analysis. The signature contains method name, return type and parameters.
8.  Then we check for the existence of the class and method in the given view.
9. If they exist, a SootClass and SootMethod objects are used to retrieve the same.
10. Then prints the body of the SootMethod object.
11. Then we check if the interceptor worked.  ie here we check if the DeadAssignmentEliminator interceptor has successfully removed a specific assignment (l1 = 3. from the method's body. It does this by looking through all statements (JAssignStmt) in the method body and checking if the assignment is not                     present.
12. Then it prints the result of the interceptor check.


### CallGraphExample
1. package sootup.examples; - defines the package name for the Java class.
2. import statement - defines various classes and interfaces from different packages that the program uses.
3. public class CallgraphExample  - declares a public class named "CallGraphExample".
4. Then we have created a main method in which is the entry point for the code.
5. List<AnalysisInputLocation> inputLocations creates a list of AnalysisInputLocation objects. These specify where Soot should look for Java class files for analysis.
6. Then we have provided towo inputLocations.add() - one for the project's class file directory and another for Java's runtime library (rt.jar).
7. Then we have created a JavaView which is used for analysing the Java program.
8. Then we have created two ClassType for two classes ie 'A' and 'B'. They are used to create a MethodSignature for a method that will be analysed.
9. ViewTypeHierarchy  - then we have set up a type hierarchy from the provided view and prints the subclasses of class 'A'.
10. Initializes a CallGraphAlgorithm using the ClassHierarchyAnalysisAlgorithm, which is a method for constructing call graphs.
11. Then we creates a call graph by initialising the Class Hierarchy Analysis (cha) with the entry method signature.
12. Prints information about calls from the entry method in the call graph.

### ClassHierarchyExample
1. package sootup.examples; - defines the package name for the Java class.
2. import statement - defines various classes and interfaces from different packages that the program uses.
3. public class ClassHierarchy - declares a public class named "ClassHierarchy".
4. Then we have created a main method in which is the entry point for the code.
5. Then creates a list of AnalysisInputLocation objects. These specify where Soot should look for Java class files for analysis. Two locations are added: one for the project's binary directory and another for the default Java runtime library (rt.jar).
6. Initializes a JavaView object with the previously created input locations.
7. Initializes a ViewTypeHierarchy object using the view. This object will be used to analyze the class hierarchy.
8. Then we have created two ClassTypes. These lines get JavaClassType objects for classes "A" and "C". These types are used for further hierarchy analysis.
9. Checks the direct subclasses of class "C". It verifies if all direct subclasses are "D" using two different methods: comparing class names and fully qualified names.
10. Then prints a message based on whether all direct subtypes of "C" are correctly identified as "D".
11. Retrieves and checks the superclasses of class "C". It then verifies if these superclasses include class "A" and java.lang.Object, printing a message based on the result.

### MutatingSootClassExample
1. package sootup.examples; - defines the package name for the Java class.
2. import statement - defines various classes and interfaces from different packages that the program uses.
3. public class MutatingSootClass - declares a public class named "MutatingSootClass".
4. Then we have created a main method in which is the entry point for the code.
5. First we have created an 'AnalysisInputLocation' which points to a directory which contains the class files to be analysed.
6. Then we have created a JavaView which allos us to retrievet the classes.
7. And also created a ClassType to get the class 'HelloWorld' and a method within that class ie main for analysis using MethodSignature.
8. THen we are checking and retrieving the class and method.
9. Then we retrives the existing body of the method and prints it. Then we create a new local variable to add it copy to the method body.
10. Then we are overriding the method body and class. ie this lines creates new sources that overrides teh original method body and class. It replaces the old method in the class with the new method having the modified body.
11. Prints the modified method body and checks if the new local variable (newLocal) exists in the modified method. Depending on the result, it prints a corresponding message.

-->