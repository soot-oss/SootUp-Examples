package sootup.examples;

import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;
import heros.flowfunc.KillAll;
import heros.flowfunc.Transfer;
import sootup.analysis.interprocedural.ifds.DefaultJimpleIFDSTabulationProblem;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.types.NullType;

import java.util.*;

public class TaintAnalysisProblem
        extends DefaultJimpleIFDSTabulationProblem<Value, InterproceduralCFG<Stmt, SootMethod>> {

    private SootMethod entryMethod;

    protected InterproceduralCFG<Stmt, SootMethod> icfg;

    public TaintAnalysisProblem(
            InterproceduralCFG<Stmt, SootMethod> icfg, SootMethod entryMethod) {
        super(icfg);
        this.icfg = icfg;
        this.entryMethod = entryMethod;
    }

    @Override
    public Map<Stmt, Set<Value>> initialSeeds() {
        return DefaultSeeds.make(
                Collections.singleton(entryMethod.getBody().getStmtGraph().getStartingStmt()), zeroValue());
    }

    @Override
    protected FlowFunctions<Stmt, Value, SootMethod> createFlowFunctionsFactory() {
        return new FlowFunctions<Stmt, Value, SootMethod>() {

            @Override
            public FlowFunction<Value> getNormalFlowFunction(Stmt curr, Stmt succ) {
                return getNormalFlow(curr, succ);
            }

            @Override
            public FlowFunction<Value> getCallFlowFunction(Stmt callStmt, SootMethod destinationMethod) {
                return getCallFlow(callStmt, destinationMethod);
            }

            @Override
            public FlowFunction<Value> getReturnFlowFunction(
                    Stmt callSite, SootMethod calleeMethod, Stmt exitStmt, Stmt returnSite) {
                return getReturnFlow(callSite, calleeMethod, exitStmt, returnSite);
            }

            @Override
            public FlowFunction<Value> getCallToReturnFlowFunction(Stmt callSite, Stmt returnSite) {
                return getCallToReturnFlow(callSite, returnSite);
            }
        };
    }

    @Override
    protected Value createZeroValue() {
        return new Local("<<zero>>", NullType.getInstance());
    }

    /**
     * NormalFlow corresponds to all intraprocedural statements, all the statements except method calls (invokes)
     *
     * @param currentStmt
     * @param successorStmt
     * @return
     */
    FlowFunction<Value> getNormalFlow(Stmt currentStmt, Stmt successorStmt) {
        if (currentStmt instanceof JAssignStmt) {
            final JAssignStmt assign = (JAssignStmt) currentStmt;
            final Value leftOp = assign.getLeftOp();
            final Value rightOp = assign.getRightOp();

            if (rightOp instanceof StringConstant) {
                StringConstant str = (StringConstant) rightOp;
                if (str.getValue().equals("SECRET")) {
                    return new Gen<>(leftOp, zeroValue());
                }
            }
            return new Transfer<>(leftOp, rightOp);
        }
        return Identity.v(); // Anything else continues as it is
    }

    /**
     * CallFlow corresponds to a method call.
     * During a method call actual arguments are mapped into the local parameters in the callee method's context.
     *
     * @param callStmt
     * @param destinationMethod
     * @return
     */
    FlowFunction<Value> getCallFlow(Stmt callStmt, final SootMethod destinationMethod) {
        AbstractInvokeExpr ie = callStmt.getInvokeExpr();
        final List<Immediate> callArgs = ie.getArgs();
        Map<Value, Value> callArgsToLocalParamsMapping = new HashMap<>(); // a map of call arguments to local parameters
        for (int i = 0; i < destinationMethod.getParameterCount(); i++) {
            callArgsToLocalParamsMapping.put(callArgs.get(i), destinationMethod.getBody().getParameterLocal(i));
        }
        for (Value callArg : callArgsToLocalParamsMapping.keySet()) {
            return new Transfer<>(callArgsToLocalParamsMapping.get(callArg), callArg);
        }

        return KillAll.v(); // Anything else should not be mapped into this context
    }

    /**
     * ReturnFlow corresponds to returning from a method call.
     * During a method return, the returned value is mapped to the caller method's context.
     *
     * @param callSite
     * @param calleeMethod
     * @param exitStmt
     * @param returnSite
     * @return
     */
    FlowFunction<Value> getReturnFlow(
            final Stmt callSite, final SootMethod calleeMethod, Stmt exitStmt, Stmt returnSite) {
        if (exitStmt instanceof JReturnStmt) {
            JReturnStmt returnStmt = (JReturnStmt) exitStmt;
            if (callSite instanceof JAssignStmt) {
                JAssignStmt assignStmt = (JAssignStmt) callSite;
                final Value retOp = returnStmt.getOp();
                if(!(retOp instanceof StringConstant)){ // normal parameter return
                    LValue leftOp = assignStmt.getLeftOp();
                    return new Transfer<>(leftOp, retOp);
                }else {
                    StringConstant str = (StringConstant) retOp;
                    if (str.getValue().equals("SECRET")) {
                        if (callSite instanceof JAssignStmt) {
                            JAssignStmt assign = (JAssignStmt) callSite;
                            final Value leftOp = assign.getLeftOp();
                            return new Gen<>(leftOp, zeroValue());
                        }
                    }
                }
            }
        }
        return KillAll.v(); // Anything else should not be returned from this context
    }

    FlowFunction<Value> getCallToReturnFlow(final Stmt callSite, Stmt returnSite) {
        return Identity.v();
    }
}