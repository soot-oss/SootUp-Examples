package sootup.examples;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

/** This example illustrates how to create and use a new Soot Project. */
public class BasicSetup {

    public static void main(String[] args) {
        // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
        // from the directory
        Path pathToBinary = Paths.get("src/test/resources/Basicsetup/binary");
        AnalysisInputLocation inputLocation = PathBasedAnalysisInputLocation.create(pathToBinary, null);

        // Create a view for project, which allows us to retrieve classes
        View view = new JavaView(inputLocation);

        // Create a signature for the class we want to analyze
        ClassType classType = view.getIdentifierFactory().getClassType("HelloWorld");

        // Create a signature for the method we want to analyze
        MethodSignature methodSignature =
                view.getIdentifierFactory()
                        .getMethodSignature(
                                classType, "main", "void", Collections.singletonList("java.lang.String[]"));

        // Check if the class "HelloWorld" is present in the project.
        if (!view.getClass(classType).isPresent()) {
            System.out.println("Class not found!");
            return;
        }

        // Retrieve the specified class from the project.
        SootClass sootClass = view.getClass(classType).get();

        // Retrieve method
        view.getMethod(methodSignature);

        if (!sootClass.getMethod(methodSignature.getSubSignature()).isPresent()) {
            System.out.println("Method not found!");
            return;  // Exit if the method is not found
        }

        // Retrieve the specified method from the class.
        SootMethod sootMethod = sootClass.getMethod(methodSignature.getSubSignature()).get();

        // Read jimple code of method
        System.out.println(sootMethod.getBody());

        // Check if the method contains a specific print statement ("Hello World!").
        boolean helloWorldPrintPresent =
                sootMethod.getBody().getStmts().stream()
                        .anyMatch(
                                stmt ->
                                        stmt instanceof JInvokeStmt
                                                && stmt.getInvokeExpr() instanceof JVirtualInvokeExpr
                                                && stmt.getInvokeExpr()
                                                .getArg(0)
                                                .equivTo(JavaJimple.getInstance().newStringConstant("Hello World!")));

        // Print the result of the check.
        if (helloWorldPrintPresent) {
            System.out.println("Hello World print is present.");
        } else {
            System.out.println("Hello World print is not present.");
        }
    }


}