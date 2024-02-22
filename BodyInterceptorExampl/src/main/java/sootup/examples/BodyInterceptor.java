package sootup.examples;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.interceptors.DeadAssignmentEliminator;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

/** This example illustrates how to invoke body interceptors. */
public class BodyInterceptor {


  public static void main(String[] args) {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    JavaLanguage language = new JavaLanguage(8);
    AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(
            "src/test/resources/BodyInterceptor/binary",
            null);

    ClassLoadingOptions clo = new ClassLoadingOptions() {
      @NotNull
      @Override
      public List<sootup.core.transform.BodyInterceptor> getBodyInterceptors() {
        return Collections.singletonList(new DeadAssignmentEliminator());
      }
    };

    // Create a project and specify the ClassLoadingOptions
    JavaProject project = JavaProject.builder(language).addInputLocation(inputLocation).build();
    JavaView view = project.createView(analysisInputLocation -> clo);

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

    SootClass<JavaSootClassSource> sootClass = view.getClass(classType).get();

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