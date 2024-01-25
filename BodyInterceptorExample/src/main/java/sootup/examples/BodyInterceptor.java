package sootup.examples;

import java.util.Collections;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.interceptors.DeadAssignmentEliminator;
import sootup.java.core.views.JavaView;

/** This example illustrates how to invoke body interceptors. */
public class BodyInterceptor {


  public static void main(String[] args) {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            "src/test/resources/BodyInterceptor/binary",
            null,
            Collections.singletonList(new DeadAssignmentEliminator()));

    // Create a new JavaView based on the input location
    JavaView view = new JavaView(inputLocation);

    // Create a signature for the class we want to analyze
    ClassType classType = view.getIdentifierFactory().getClassType("File");

    // Create a signature for the method we want to analyze
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(classType, "someMethod", "void", Collections.emptyList());

    // Check if class is present
    if (!view.getClass(classType).isPresent()) {
      System.out.println("Class not found.");
      return;
    }

    SootClass sootClass = view.getClass(classType).get();

    // Retrieve method
    if (!view.getMethod(methodSignature).isPresent()) {
      System.out.println("Method not found.");
      return;
    }
    SootMethod method = view.getMethod(methodSignature).get();

    System.out.println(method.getBody());

    // Check if l1 = 3 is not present, i.e., body interceptor worked
    boolean interceptorWorked =
        method.getBody().getStmts().stream()
            .noneMatch(
                stmt ->
                    stmt instanceof JAssignStmt
                        && ((JAssignStmt) stmt).getRightOp().equivTo(IntConstant.getInstance(3)));

    if (interceptorWorked) {
      System.out.println("Interceptor worked as expected.");
    } else {
      System.out.println("Interceptor did not work as expected.");
    }
  }
}
