package sootup.java.core.exceptions;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2025 Zun Wang
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.*;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An analyzer for a <code>Stmt</code> to determine the exceptions it might throw.
 */

public class StmtExceptionAnalyser {

    private final TypeHierarchy hierarchy;

    public StmtExceptionAnalyser(TypeHierarchy hierarchy){
        this.hierarchy = hierarchy;
    }

    public ExceptionInferResult mightThrow(@Nonnull Stmt stmt, @Nonnull MutableStmtGraph graph){
        if(stmt instanceof JThrowStmt){
            return mightThrowExplicitly((JThrowStmt) stmt, graph);
        }else {
            return mightThrowImplicitly(stmt);
        }
    }

    public ExceptionInferResult mightThrowExplicitly(@Nonnull JThrowStmt throwStmt, @Nonnull MutableStmtGraph graph){
        Immediate throwExpression = throwStmt.getOp();
        //todo: needs to check if all throwExpressions are locals
        if(!(throwExpression instanceof Local)){
            throw new IllegalStateException("The given throwStmt: \"" + throwStmt +"\" doesn't throw a local!");
        }
        Local exceptionLocal = (Local) throwExpression;
        Type throwType = exceptionLocal.getType();
        if(throwType == null || throwType instanceof UnknownType){
            return ExceptionInferResult.createThrowableExceptions();
        }
        if(throwType instanceof NullType){
            return ExceptionInferResult.createNullPointerException();
        }
        if(!(throwType instanceof ClassType)){
            throw new IllegalStateException("The type of " + throwStmt +" is not a ClassType!");
        }
        Type preciserType = findPreciserType(exceptionLocal,graph);
        if(preciserType != null){
            throwType = preciserType;
        }
        if(!(preciserType instanceof ClassType)){
            throw new IllegalStateException("The type of " + preciserType +" is not a ClassType!");
        }
        return ExceptionInferResult.createSingleException((ClassType) throwType, hierarchy);
    }

    private Type findPreciserType(@Nonnull Local local, @Nonnull MutableStmtGraph graph){
        Type preciserType = null;
        Set<Stmt> defStmtsOfLocal = graph.getStmts().stream().filter(stmt -> stmt instanceof AbstractDefinitionStmt).filter(stmt -> ((AbstractDefinitionStmt) stmt).getLeftOp()==local).collect(Collectors.toSet());
        Set<Value> aliasesOfLocal = defStmtsOfLocal.stream().map(stmt -> ((AbstractDefinitionStmt) stmt).getRightOp()).collect(Collectors.toSet());
        Set<Type> allocationTypes = aliasesOfLocal.stream().filter(value -> value instanceof JNewExpr).map(value -> value.getType()).collect(Collectors.toSet());
        if(allocationTypes.size() == 1){
            preciserType = allocationTypes.iterator().next();
        }
        return preciserType;
    }

    private ExceptionInferResult mightThrowImplicitly(Stmt stmt){
        ExceptionInferResult result = ExceptionInferResult.createEmptyException();
        if(stmt instanceof JAssignStmt){

            Value leftOp = ((JAssignStmt) stmt).getLeftOp();
            Value rightOp = ((JAssignStmt) stmt).getRightOp();

            //write in an Array
            if(leftOp instanceof JArrayRef){
                result.addException(ExceptionInferResult.ExceptionType.INDEX_OUT_OF_BOUNDS_EXCEPTION, hierarchy);
                result.addException(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION, hierarchy);
                if(rightOp instanceof ClassType){
                    result.addException(ExceptionInferResult.ExceptionType.ARRAY_STORE_EXCEPTION, hierarchy);
                }
                return result;
            }

            //read from an Array
            if(rightOp instanceof JArrayRef){
                result.addException(ExceptionInferResult.ExceptionType.INDEX_OUT_OF_BOUNDS_EXCEPTION, hierarchy);
                result.addException(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION, hierarchy);
                return result;
            }
        }
        return result;
    }
}
