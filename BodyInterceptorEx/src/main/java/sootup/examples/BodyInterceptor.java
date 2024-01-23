package sootup.examples;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.bytecode.interceptors.DeadAssignmentEliminator;
//import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

/** This example illustrates how to invoke body interceptors. */
public class BodyInterceptor {
  public static void main(String[] args) {
    // Create an instance of BodyInterceptor
    BodyInterceptor interceptor = new BodyInterceptor();

    // Call the test method
    interceptor.test();
  }

  public void test() {
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
    SootClass sootClass = view.getClass(classType);
    if (sootClass == null) {
      System.err.println("Class not found");
      return;
    }






    // Configure body interceptors for the project. This example uses DeadAssignmentEliminator.
    view.configBodyInterceptors(
        analysisInputLocation ->
            new ClassLoadingOptions() {
              @Nonnull
              @Override
              public List<sootup.core.transform.BodyInterceptor> getBodyInterceptors() {
                return Collections.singletonList(new DeadAssignmentEliminator());
              }
            });

    // Check if the specified class is present in the project.
    if (!view.getClass(classType).isPresent()) {
      System.out.println("Class not found!");
      return;
    }

    // Retrieve the specified class from the project.
    SootClass<JavaSootClassSource> sootClass = view.getClass(classType).get();

    // Check if the specified method is present in the retrieved class.
    if (!view.getMethod(methodSignature).isPresent()) {
      System.out.println("Method not found!");
      return;
    }

    // Retrieve the method for analysis.
    SootMethod method = view.getMethod(methodSignature).get();

    // Print the body of the method.
    System.out.println(method.getBody());

    // Check if a specific assignment (l1 = 3) has been eliminated in the method's body.
    boolean isAssignmentEliminated =
        method.getBody().getStmts().stream()
            .noneMatch(
                stmt ->
                    stmt instanceof JAssignStmt
                        && ((JAssignStmt) stmt).getRightOp().equivTo(IntConstant.getInstance(3)));

    // Print the result of the dead assignment elimination check.
    if (isAssignmentEliminated) {
      System.out.println("Dead assignment eliminated.");
    } else {
      System.out.println("Dead assignment not eliminated.");
    }
  }
}
