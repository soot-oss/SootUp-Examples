package sootup.examples;

import heros.InterproceduralCFG;
import sootup.analysis.interprocedural.ifds.JimpleIFDSSolver;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.examples.targetcode.BasicTaint;
import sootup.examples.targetcode.BasicTaintSanitized;
import sootup.examples.targetcode.FunctionPropagatesTaint;
import sootup.examples.targetcode.FunctionReturnsTaint;

public class TaintAnalysisExample {

    public static void main(String[] args){
        // intraprocedural taint
        analyze(BasicTaint.class.getName());

        // intraprocedural taint sanitized
        analyze(BasicTaintSanitized.class.getName());

        // interprocedural taint propagated
        analyze(FunctionPropagatesTaint.class.getName());

        // interprocedural taint returned
        analyze(FunctionReturnsTaint.class.getName());
    }

    public static void analyze(String className){
        String pathToTarget = "target/classes";
        AnalysisRunner runner = new AnalysisRunner();
        JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis =
                runner.executeStaticAnalysis(pathToTarget, className);
        runner.checkLeak(analysis);
    }
}
