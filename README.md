# SootUp-Examples
Example code to help getting start with SootUp


1) Here we will provide some examples that uses SootUp to provide insights about a Java program. The repository that contains the examples can be found in https://github.com/soot-oss/SootUp-Examples.git.
2) There are mainly 5 projects to be considered under SootUp.
   a) BasicSetupExample
   b) BodyInterceptorExample
   c) CallGraphExample
   d) ClassHierarchyExample
   e) MutatingSootClassExample

3) We have included all the five projects in 5 different branches under SootUp-Examples with detailed explanation about the project.

a) BasicSetupExample
---------------------

      1) package sootup.examples; - defines the package name for the Java class.
      2) import statement - defines various classes and interfaces from different packages that the program uses.
      3) public class BasicSetup - declares a public class named 'BasicSetup' which is the main class for this program.
      4) public void createByteCodeProject() - declares a public method named 'createByteCodeProject'. 
      5) Path pathToBinary = Paths.get("src/test/resources/Basicsetup/binary"); - creates a 'Path' object which points to a directory that contains a class files.
      6) AnalysisInputLocation inputLocation = PathBasedAnalysisInputLocation.create(pathToBinary, null); - Sets the location for the SootUp analysis, indicating where the source code is located.
      7) View view = new JavaView(inputLocation); - creates a 'View' for the analysis, using the input location defined before. 
      8) ClassType and MethodSignature - The ClassType creates an identifier for a specific class ie HelloWorld and a method within the class using MethodSignature (main class). The method signature includes the method name, return type and parameters.
      9) Then we have provided an if condition, ie used to check the existence of the specified class.
      10) Then we have used SootClass for retrieving the class that is present.
      11) Then we are checking the existence of the method. getSubSignature() is a method that returns a string representation of the method signature, which SootUp uses to identify methods.
      12) SootMethod - this line retrieves the method.
      13) Then it prints the jimple representation of the method's body.
      14) The next if condition checks if the method containts a specific statement called 'Hello World!'.
      15) Then we have created a main method to create the instance of BasicSetup class and calls the method createByteCodeProject using that instance.

This code is used to demonstrate the basic operations like setting up the analysis environment, retrieving the classes and methods and performing check on the method content.

b) BodyInterceptorExample
------------------------

      1) package sootup.examples; - defines the package name for the Java class.
      2) import statement - defines various classes and interfaces from different packages that the program uses.
      3) public class BodyInterceptor - declares a public class named "BodyInterceptor".
      4) Then we have created a main method in which we have created an instance for the class "BodyInterceptor" using which we are calling other methods of the class ie here it is test() mehtod.
      5) Then we have declared a test method.
      6) Inside the test method, we have created an AnalysisInputLocation object which specifies a path to look for the Java class files to analyse.
      7) Then we have initialized a JavaView to anslyse the Java program.
      8) Then have created a ClassType and MethodSignature which is used for analysis. The signature contains method name, return type and parameters.
      9) Then we check for the existence of the class and method in the given view.
      10) If they exist, a SootClass and SootMethod objects are used to retrieve the same.
      11) Then prints the body of the SootMethod object. 
      12) Then we check if the interceptor worked.  ie here we check if the DeadAssignmentEliminator interceptor has successfully removed a specific assignment (l1 = 3) from the method's body. It does this by looking through all statements (JAssignStmt) in the method body and checking if the assignment is not present.
      13) Then it prints the result of the interceptor check.

This code demonstrates the use of DeadAssignmentEliminator interceptor to modify the method's bytecode, ie to optimise it by removing dead assignments. 


c) CallGraphExample
-------------------

      1) package sootup.examples; - defines the package name for the Java class.
      2) import statement - defines various classes and interfaces from different packages that the program uses.
      3) public class CallgraphExample - Then we have declared a public class named 'CallgraphExample'.
      4) Within the same class, we have declared a 'test()' method.
      5) Within in the test method, List<AnalysisInputLocation> inputLocations creates a list of AnalysisInputLocation objects. These specify where Soot should look for Java class files for analysis. 
      6) Then we have provided towo inputLocations.add() - one for the project's class file directory and another for Java's runtime library (rt.jar).
      7) Then we have created a JavaView which is used for analysing the Java program.
      8) Then we have created two ClassType for two classes ie 'A' and 'B'. They are used to create a MethodSignature for a method that will be analysed.
      9) ViewTypeHierarchy  - then we have set up a type hierarchy from the provided view and prints the subclasses of class 'A'.
      10) Initializes a CallGraphAlgorithm using the ClassHierarchyAnalysisAlgorithm, which is a method for constructing call graphs.
      11) Then we creates a call graph by initialising the Class Hierarchy Analysis (cha) with the entry method signature.
      12) Prints information about calls from the entry method in the call graph.
      13) The main method, which is the entry point of the program. It creates an instance of CallgraphExample and calls the test method.

A call graph is a representation of calling relationships between subroutines in a program. The program specifically sets up input locations for Soot to analyze, creates type information for specific classes, and then uses this information to construct a call graph based on the method signatures. Finally, it prints out information from the call graph related to specific methods.


d) ClassHierarchyExample
------------------------

      1) package sootup.examples; - defines the package name for the Java class.
      2) import statement - defines various classes and interfaces from different packages that the program uses.
      3) public class ClassHierarchy - declares a public class named 'ClassHierarchy'.
      4) public void test() - Defines a public method named test.
      5) Then creates a list of AnalysisInputLocation objects. These specify where Soot should look for Java class files for analysis. Two locations are added: one for the project's binary directory and another for the default Java runtime library (rt.jar).
      6) Initializes a JavaView object with the previously created input locations. 
      7) Initializes a ViewTypeHierarchy object using the view. This object will be used to analyze the class hierarchy.
      8) Then we have created two ClassTypes. These lines get JavaClassType objects for classes "A" and "C". These types are used for further hierarchy analysis.
      9) Checks the direct subclasses of class "C". It verifies if all direct subclasses are "D" using two different methods: comparing class names and fully qualified names.
      10) Then prints a message based on whether all direct subtypes of "C" are correctly identified as "D".
      11) Retrieves and checks the superclasses of class "C". It then verifies if these superclasses include class "A" and java.lang.Object, printing a message based on the result.
      12)  The main method, which is the entry point of the program. It creates an instance of ClassHierarchy and calls the test method.

This code sets up the environment for analysis, specifies classes to analyze, and then checks their hierarchical relationships (subclasses and superclasses). 


   e) MutatingSootClassExample
   ---------------------------

      1) package sootup.examples; - defines the package name for the Java class.
      2) import statement - defines various classes and interfaces from different packages that the program uses.
      3) public class MutatingSootClass - declares a public class named 'MutatingSootClass'.
      4) Then we have created the main method within which we have provided the entire code.
      5) First we have created an 'AnalysisInputLocation' which points to a directory which contains the class files to be analysed.
      6) Then we have created a JavaView which allos us to retrievet the classes.
      7) And also created a ClassType to get the class 'HelloWorld' and a method within that class ie main for analysis using MethodSignature.
      8) THen we are checking and retrieving the class and method.
      9) Then we retrives the existing body of the method and prints it. Then we create a new local variable to add it copy to the method body.
      10) Then we are overriding the method body and class. ie this lines creates new sources that overrides teh original method body and class. It replaces the old method in the class with the new method having the modified body.
      11) Prints the modified method body and checks if the new local variable (newLocal) exists in the modified method. Depending on the result, it prints a corresponding message.


The program demonstrates modifying a class and its method's bytecode using SootUp. It specifically alters a method by adding a new local variable and then checking if this change is reflected in the class.



      

      
      



      
