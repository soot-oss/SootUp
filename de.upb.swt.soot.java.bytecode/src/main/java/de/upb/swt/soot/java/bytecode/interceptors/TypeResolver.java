package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.LocalGenerator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.AbstractBinopExpr;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNegExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;

import java.util.*;

public class TypeResolver {
    private  final Body.BodyBuilder builder;
    private Map<Integer, AbstractDefinitionStmt> id2assignments = new HashMap<>();
    private final Map<Local, BitSet> depends = new HashMap<>();
    private final LocalGenerator localGenerator;

    public TypeResolver(Body.BodyBuilder builder){
        this.builder = builder;
        //todo: maybe use withNewLocal(), not the local Generator
        localGenerator = new LocalGenerator(builder.getLocals());
        init();
    }

    /**
     * observe all assignments, add all locals at right-hand-side into the map depends
     */
    private void init(){
        int assignID = 0;
        for(Stmt stmt : builder.getStmts()){
            if(stmt instanceof AbstractDefinitionStmt){
                AbstractDefinitionStmt defStmt = (AbstractDefinitionStmt) stmt;
                Value lhs = defStmt.getLeftOp();
                if(lhs instanceof Local || lhs instanceof JArrayRef){
                    this.id2assignments.put(assignID, defStmt);
                    addDependsForRHS(defStmt.getRightOp(), assignID);
                    assignID++;
                }
            }
        }
    }

    private void addDependsForRHS(Value value, int id){
        if(value instanceof Local){
            addDepend((Local) value, id);
        }else if(value instanceof AbstractBinopExpr){
            Immediate op1 = ((AbstractBinopExpr) value).getOp1();
            Immediate op2 = ((AbstractBinopExpr) value).getOp2();
            if(op1 instanceof Local){
                addDepend((Local) op1, id);
            }
            if(op2 instanceof Local){
                addDepend((Local) op2, id);
            }
        }else if(value instanceof JNegExpr){
            Immediate op = ((JNegExpr) value).getOp();
            if(op instanceof Local){
                addDepend((Local) op, id);
            }
        } else if(value instanceof JCastExpr){
            Immediate op = ((JCastExpr) value).getOp();
            if(op instanceof Local){
                addDepend((Local) op, id);
            }
        }else if(value instanceof JArrayRef){
            Local base = ((JArrayRef) value).getBase();
            addDepend(base, id);
        }
    }

    private  void addDepend(Local local, int id){
        BitSet bitSet = this.depends.get(local);
        if(bitSet == null){
            bitSet = new BitSet();
            this.depends.put(local, bitSet);
        }
        bitSet.set(id);
    }
}

