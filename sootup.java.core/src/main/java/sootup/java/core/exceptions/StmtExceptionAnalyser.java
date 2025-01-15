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
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.types.NullType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;

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

    public ExceptionAnalyserResult mightThrow(@Nonnull Stmt stmt, @Nonnull MutableStmtGraph graph){
        if(stmt instanceof JThrowStmt){
            return mightThrowExplicitly((JThrowStmt) stmt, graph);
        }else {
            return mightThrowImplicitly(stmt);
        }
    }

    public ExceptionAnalyserResult mightThrowExplicitly(@Nonnull JThrowStmt throwStmt, @Nonnull MutableStmtGraph graph){
        Immediate throwExpression = throwStmt.getOp();
        //todo: needs to check if all throwExpressions are locals
        if(!(throwExpression instanceof Local)){
            throw new IllegalStateException("The given throwStmt: \"" + throwStmt +"\" doesn't throw a local!");
        }
        Local exceptionLocal = (Local) throwExpression;
        Type throwType = exceptionLocal.getType();
        if(throwType == null || throwType instanceof UnknownType){
            return ExceptionAnalyserResult.createThrowableExceptions();
        }
        if(throwType instanceof NullType){
            return ExceptionAnalyserResult.createNullPointerException();
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
        return ExceptionAnalyserResult.createSingleException((ClassType) throwType, hierarchy);
    }

    private Type findPreciserType(@Nonnull Local local, @Nonnull MutableStmtGraph graph){
        Type preciserType = null;
        Set<Stmt> defStmts = graph.getStmts().stream().filter(stmt -> stmt instanceof AbstractDefinitionStmt).collect(Collectors.toSet());
        Set<Value> aliasesOfGivenLocal = defStmts.stream().filter(stmt -> ((AbstractDefinitionStmt) stmt).getLeftOp()==local).map(stmt -> ((AbstractDefinitionStmt) stmt).getRightOp()).collect(Collectors.toSet());
        Set<Type> allocationTypes = aliasesOfGivenLocal.stream().filter(value -> value instanceof JNewExpr).map(value -> value.getType()).collect(Collectors.toSet());
        if(allocationTypes.size() == 1){
            preciserType = allocationTypes.iterator().next();
        }
        return preciserType;
    }

    private ExceptionAnalyserResult mightThrowImplicitly(Stmt stmt){
        return null;
    }
}
