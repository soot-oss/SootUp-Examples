package sootup.examples;

import java.nio.file.Paths;
import java.util.Collections;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType.IntType;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

/**
 * This example shows how to change a method inside a SootClass using OverridingBodySources and
 * OverridingClassSources.*
 */
public class MutatingSootClass {

  public static void main(String[] args) {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    AnalysisInputLocation<JavaSootClass> inputLocation =
        PathBasedAnalysisInputLocation.create(
            Paths.get("src/test/resources/Mutatingclasssoot/binary"), null);

    // Specify the language of the JavaProject. This is especially relevant for Multi-release jars,
    // where classes are loaded depending on the language level of the analysis
    JavaLanguage language = new JavaLanguage(8);

    // Create a new JavaProject based on the input location
    JavaProject project = JavaProject.builder(language).addInputLocation(inputLocation).build();

    // Create a signature for the class we want to analyze
    ClassType classType = project.getIdentifierFactory().getClassType("HelloWorld");

    // Create a signature for the method we want to analyze
    MethodSignature methodSignature =
        project
            .getIdentifierFactory()
            .getMethodSignature(
                classType, "main", "void", Collections.singletonList("java.lang.String[]"));

    // Create a view for project, which allows us to retrieve classes
    JavaView view = project.createView();

    // Retrieve class
    SootClass<JavaSootClassSource> sootClass = view.getClass(classType).get();

    // Retrieve the specified method from the class.
    SootMethod method = view.getMethod(methodSignature).get();
    Body oldBody = method.getBody();

    // Print the original method body.
    System.out.println(oldBody);

    // Create OverridingBodySource
    //    OverridingBodySource overridingBodySource =
    //            new OverridingBodySource(methodSignature, method.getBody());

    // Local variables and method body modifications are commented out.

    // Create a local variable of type 'int'.
    Local newLocal = JavaJimple.newLocal("helloWorldLocal", IntType.getInt());

    // Create a new method body with the added local variable.
    Body newBody = oldBody.withLocals(Collections.singleton(newLocal));

    // Modify the method body source with the new body.
    OverridingBodySource newBodySource =
        new OverridingBodySource(method.getBodySource()).withBody(newBody);

    // Create an overriding class source for the retrieved class
    OverridingJavaClassSource overridingJavaClassSource =
        new OverridingJavaClassSource(sootClass.getClassSource());

    // Create a new method with the modified body.
    SootMethod newMethod = method.withOverridingMethodSource(old -> newBodySource);

    // Replace the original method in the class source with the new method.
    OverridingJavaClassSource newClassSource =
        overridingJavaClassSource.withReplacedMethod(method, newMethod);

    // Create a new class with the modified class source.
    SootClass<JavaSootClassSource> newClass = sootClass.withClassSource(newClassSource);

    // Print the body of the first method in the modified class.
    System.out.println(newClass.getMethods().stream().findFirst().get().getBody());
  }
}
