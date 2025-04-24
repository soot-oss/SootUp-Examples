package sootup.examples;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

public class TypestateChecker {

    public static void main(String[] args) {
        // Use the directory where your classes are compiled
        Path classPath = Paths.get("target/classes");

        System.out.println("Using classpath: " + classPath.toAbsolutePath());

        if (!classPath.toFile().exists()) {
            System.err.println("Error: The classpath directory does not exist: " + classPath.toAbsolutePath());
            return;
        }

        AnalysisInputLocation inputLocation =
                new JavaClassPathAnalysisInputLocation(classPath.toString());
        JavaView view = new JavaView(inputLocation);

        ClassType declaringClass =
                view.getIdentifierFactory().getClassType("typestateanalysis.example.TypestateTestProgram");

        Type stringArrayType =
                view.getIdentifierFactory()
                        .getArrayType(view.getIdentifierFactory().getClassType("java.lang.String"), 1);

        MethodSignature methodSignature =
                view.getIdentifierFactory()
                        .getMethodSignature(
                                declaringClass,
                                "main",
                                view.getIdentifierFactory().getType("void"),
                                List.of(stringArrayType));

        Optional<JavaSootMethod> methodOpt = view.getMethod(methodSignature);

        if (methodOpt.isEmpty()) {
            System.err.println("Method not found. Check class path or signature.");
            return;
        }

        SootMethod method = methodOpt.get();
        System.out.println("Analyzing method: " + method.getSignature());

        for (Stmt stmt : method.getBody().getStmts()) {
            System.out.println("Statement: " + stmt);
            System.out.println("Statement type: " + stmt.getClass().getName());

            try {
                if (hasMethod(stmt.getClass(), "getInvokeExpr")) {
                    java.lang.reflect.Method getInvokeExprMethod = stmt.getClass().getMethod("getInvokeExpr");
                    Object result = getInvokeExprMethod.invoke(stmt);

                    if (result instanceof Optional<?>) {
                        Optional<?> optional = (Optional<?>) result;
                        if (optional.isPresent() && optional.get() instanceof AbstractInvokeExpr) {
                            handleInvoke((AbstractInvokeExpr) optional.get());
                        }
                    } else if (result instanceof AbstractInvokeExpr) {
                        handleInvoke((AbstractInvokeExpr) result);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error processing statement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static boolean hasMethod(Class<?> clazz, String methodName) {
        try {
            clazz.getMethod(methodName);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean calledFoo = false;
    private static boolean calledBar = false;

    private static void handleInvoke(AbstractInvokeExpr invokeExpr) {
        String methodName = invokeExpr.getMethodSignature().getName();
        System.out.println("Invoked: " + methodName);

        if (methodName.equals("foo")) {
            calledFoo = true;
        } else if (methodName.equals("bar")) {
            if (!calledFoo) {
                System.out.println("ERROR: bar() called before foo()");
            } else {
                System.out.println("Correct sequence: foo() → bar()");
            }
            calledBar = true;
        } else if (methodName.equals("sth")) {
            if (!calledBar) {
                System.out.println("ERROR: sth() called before bar()");
            } else if (!calledFoo) {
                System.out.println("ERROR: sth() called but foo() was never called");
            } else {
                System.out.println("Correct sequence: foo() → bar() → sth()");
            }
        }
    }
}
