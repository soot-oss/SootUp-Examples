package sootup.examples;

import java.nio.file.Paths;
import java.util.Collections;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType.IntType;
import sootup.core.types.VoidType;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * This example shows how to change a method inside a SootClass using OverridingBodySources and
 * OverridingClassSources.*
 */
public class MutatingSootClass {

  public static void main(String[] args) {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    AnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.create(
            Paths.get("src/test/resources/Mutatingclasssoot/binary"), null);

    // Create a view for project, which allows us to retrieve classes
    JavaView view = new JavaView(inputLocation);

    // Create a signature for the class we want to analyze
    JavaClassType classType = view.getIdentifierFactory().getClassType("HelloWorld");

    // Create a signature for the method we want to analyze
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                classType, "main", "void", Collections.singletonList("java.lang.String[]"));

    // Check that class is present
    if (!view.getClass(classType).isPresent()) {
      System.out.println("Class not found.");
      return;
    }

    // Retrieve class
    JavaSootClass sootClass = view.getClass(classType).get();

    // Check and retrieve method
    if (!view.getMethod(methodSignature).isPresent()) {
      System.out.println("Method not found.");
      return;
    }
    JavaSootMethod method = view.getMethod(methodSignature).get();
    Body oldBody = method.getBody();

    System.out.println(oldBody);
    Local newLocal = JavaJimple.newLocal("helloWorldLocal", IntType.getInt());

    Body newBody = oldBody.withLocals(Collections.singleton(newLocal));

    OverridingBodySource newBodySource =
        new OverridingBodySource(method.getBodySource()).withBody(newBody);

    OverridingJavaClassSource overridingJavaClassSource =
        new OverridingJavaClassSource(sootClass.getClassSource());

    JavaSootMethod newMethod = method.withOverridingMethodSource(old -> newBodySource);

    OverridingJavaClassSource newClassSource =
        overridingJavaClassSource.withReplacedMethod(method, newMethod);
    SootClass newClass = sootClass.withClassSource(newClassSource);

    System.out.println(newClass.getMethods().stream().findFirst().get().getBody());

    if (newClass
        .getMethod(
            new MethodSubSignature(
                "main",
                Collections.singletonList(
                    new ArrayType(new JavaClassType("String", new PackageName("java.lang")), 1)),
                VoidType.getInstance()))
        .get().getBody().getLocals().stream()
        .anyMatch(local -> local.equals(newLocal))) {
      System.out.println("New local exists in the modified method.");
    } else {
      System.out.println("New local does not exist in the modified method.");
    }
  }
}
