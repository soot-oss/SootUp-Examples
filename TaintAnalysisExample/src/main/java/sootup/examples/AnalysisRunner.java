package sootup.examples;

import heros.InterproceduralCFG;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.analysis.interprocedural.ifds.JimpleIFDSSolver;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.interceptors.TypeAssigner;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.*;
import java.util.stream.Collectors;

public class AnalysisRunner {

    protected JavaView view;
    protected MethodSignature entryMethodSignature;
    protected SootMethod entryMethod;
    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

    private static JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> solved = null;

    protected JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> executeStaticAnalysis(String pathToJar,
                                                                                              String targetTestClassName) {
        setupSoot(pathToJar, targetTestClassName);
        runAnalysis();
        if (solved == null) {
            throw new NullPointerException("Something went wrong solving the IFDS problem!");
        }
        return solved;
    }

    private void runAnalysis() {

        JimpleBasedInterproceduralCFG icfg =
                new JimpleBasedInterproceduralCFG(view, entryMethodSignature, false, false);
        TaintAnalysisProblem problem = new TaintAnalysisProblem(icfg, entryMethod);
        JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> solver =
                new JimpleIFDSSolver(problem);
        solver.solve(entryMethod.getDeclaringClassType().getClassName());
        solved = solver;
    }

    /*
     * This method provides the options to soot to analyse the respective
     * classes.
     */
    private void setupSoot(String pathToJar, String targetTestClassName) {
        List<AnalysisInputLocation> inputLocations = new ArrayList<>();
        inputLocations.add(
                new JavaClassPathAnalysisInputLocation(
                        pathToJar, SourceType.Application, Arrays.asList(new TypeAssigner())));

        view = new JavaView(inputLocations);


        JavaClassType mainClassSignature = identifierFactory.getClassType(targetTestClassName);
        SootClass sc = view.getClass(mainClassSignature).get();
        entryMethod =
                sc.getMethods().stream().filter(e -> e.getName().equals("entryPoint")).findFirst().get();
        entryMethodSignature = entryMethod.getSignature();
        System.out.println("*******************************************************");
        System.out.println("Taint analysis starts from: " + entryMethodSignature);
    }

    public Set<Value> taintedVariablesAtSink(
            JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis) {
        Stmt sinkMethod = entryMethod.getBody().getStmts().stream().filter(s -> s instanceof JInvokeStmt).filter(s -> s.getInvokeExpr().getMethodSignature().getName().equals("sink")).findFirst().get();
        Set<?> rawSet = analysis.ifdsResultsAt(sinkMethod);
        Set<Value> names = new HashSet<>();
        for (Object fact : rawSet) {
            if (fact instanceof Local) {
                Local l = (Local) fact;
                names.add(l);
            }
            if (fact instanceof JInstanceFieldRef) {
                JInstanceFieldRef ins = (JInstanceFieldRef) fact;
                names.add(ins);
            }
            if (fact instanceof JStaticFieldRef) {
                JStaticFieldRef stat = (JStaticFieldRef) fact;
                names.add(stat);
            }
        }
        return names;
    }

    public SootMethod getMethod(String className, String methodName) {
        Optional<JavaSootClass> javaSootClassOpt = view.getClasses().stream().filter(c -> c.getName().contains(className)).findFirst();
        if (!javaSootClassOpt.isPresent()) {
            throw new RuntimeException("Class not found: " + className);
        }
        Optional<JavaSootMethod> javaSootMethodOpt = javaSootClassOpt.get().getMethods().stream().filter(m -> m.getName().equals(methodName)).findFirst();
        if (!javaSootMethodOpt.isPresent()) {
            throw new RuntimeException("Method not found: " + methodName);
        }
        return javaSootMethodOpt.get();
    }


    public void checkLeak(JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis) {
        Set<Value> taintedVars = taintedVariablesAtSink(analysis);
        System.out.println("Tainted variables at sink: " + System.lineSeparator() + taintedVars.stream().map(Objects::toString).collect(Collectors.joining(", ")));

        AbstractInvokeExpr sinkMethod = entryMethod.getBody().getStmts().stream().filter(s -> s instanceof JInvokeStmt).filter(s -> s.getInvokeExpr().getMethodSignature().getName().equals("sink")).findFirst().get().getInvokeExpr();
        Immediate arg = sinkMethod.getArgs().get(0);
        Value lastAssignment = arg;
        if (arg.toString().contains("stack")) {
            lastAssignment = getLastAssignment(arg);
        }

        System.out.println("Leaked variable at sink:");
        Value leaked = null;
        for (Value taintedVar : taintedVars) {
            if (lastAssignment.equivTo(taintedVar)) {
                leaked = taintedVar;
            }
        }
        if (leaked == null) {
            System.out.println("None");
        } else {
            System.out.println(leaked);
        }

        System.out.println("*******************************************************");
        System.out.println();
    }

    Value getLastAssignment(Value stackVar) {
        List<Stmt> stmts = entryMethod.getBody().getStmts();
        Collections.reverse(stmts);
        for (Stmt stmt : stmts) {
            if (stmt instanceof AbstractDefinitionStmt) {
                AbstractDefinitionStmt def = (AbstractDefinitionStmt) stmt;
                if (def.getLeftOp().equals(stackVar)) {
                    return def.getRightOp();
                }
            }
        }
        throw new RuntimeException("Var not found: " + stackVar);
    }

}