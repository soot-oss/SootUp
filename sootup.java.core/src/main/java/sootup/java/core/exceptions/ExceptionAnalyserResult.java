package sootup.java.core.exceptions;

import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaIdentifierFactory;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class ExceptionAnalyserResult {

    private final Set<ClassType> exceptions;

    public ExceptionAnalyserResult (Set<ClassType> exceptions){
        this.exceptions = ImmutableUtils.immutableSetOf(exceptions);
    }

    static ExceptionAnalyserResult createThrowableExceptions (){
        return new ExceptionAnalyserResult(ImmutableUtils.immutableSet(ExceptionType.THROWABLE));
    }

    static ExceptionAnalyserResult createNullPointerException(){
        return new ExceptionAnalyserResult(ImmutableUtils.immutableSet(ExceptionType.NUll_POINTER_EXCEPTION));
    }

    static ExceptionAnalyserResult createSingleException(@Nonnull ClassType exceptionType, @Nonnull TypeHierarchy typeHierarchy){
        if(!typeHierarchy.contains(exceptionType)){
            throw new IllegalArgumentException("The given exceptionType \"" + exceptionType + "\" is not in type hierarchy!");
        }
        return new ExceptionAnalyserResult(ImmutableUtils.immutableSet(exceptionType));
    }

    static ExceptionAnalyserResult createEmptyException(){
        return new ExceptionAnalyserResult(ImmutableUtils.emptyImmutableSet());
    }

    public Set<ClassType> getExceptions(){
        return this.exceptions;
    }

    protected ExceptionAnalyserResult addException(ClassType newException, TypeHierarchy typeHierarchy){
        if(!typeHierarchy.contains(newException)){
            throw new IllegalArgumentException("The given exceptionType \"" + newException + "\" is not in type hierarchy!");
        }
        Set<ClassType> resultSet = new HashSet<>(this.exceptions);
        for(ClassType exception : exceptions){
            if(exception.equals(newException) || typeHierarchy.isSubtype(exception, newException)){
                return this;
            }
            if(typeHierarchy.isSubtype(newException, exception)){
                resultSet.remove(exception);
            }
        }
        resultSet.add(newException);
        return new ExceptionAnalyserResult(resultSet);
    }

    public static class ExceptionType{
        static final ClassType THROWABLE = JavaIdentifierFactory.getInstance().getClassType("java.lang.Throwable");
        static final ClassType NUll_POINTER_EXCEPTION = JavaIdentifierFactory.getInstance().getClassType("java.lang.NullPointerException");

    }
}


