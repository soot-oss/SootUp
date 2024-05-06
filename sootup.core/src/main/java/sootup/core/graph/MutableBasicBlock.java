package sootup.core.graph;

import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MutableBasicBlock extends BasicBlock<MutableBasicBlock> {

    void addStmt(@Nonnull Stmt newStmt);

    void removeStmt(@Nonnull Stmt stmt);

    void replaceStmt(Stmt oldStmt, Stmt newStmt);


    void addPredecessorBlock(@Nonnull MutableBasicBlock block);

    void linkSuccessor(int successorIdx, MutableBasicBlock blockB);

    boolean removePredecessorBlock(@Nonnull MutableBasicBlock b);

    void setSuccessorBlock(int successorIdx, @Nullable MutableBasicBlock block);

    void removePredecessorFromSuccessorBlock(@Nonnull MutableBasicBlock b);

    void linkExceptionalSuccessorBlock(@Nonnull ClassType exception, MutableBasicBlock b);

    void removeExceptionalSuccessorBlock(@Nonnull ClassType exception);

    @Nonnull
    MutableBasicBlockImpl splitBlockLinked(int splitIdx);

    void copyExceptionalFlowFrom(MutableBasicBlock sourceBlock);


    MutableBasicBlock splitBlockUnlinked(@Nonnull Stmt newTail, @Nonnull Stmt newHead);

    MutableBasicBlockImpl splitBlockUnlinked(int splitIdx);

    @Nonnull
    MutableBasicBlock splitBlockLinked(@Nonnull Stmt splitStmt, boolean shouldBeNewHead);

    void clearSuccessorBlocks();

    void clearExceptionalSuccessorBlocks();

    void clearPredecessorBlocks();

    List<Integer> replaceSuccessorBlock(
            @Nonnull MutableBasicBlock oldBlock, @Nullable MutableBasicBlock newBlock);

    boolean replacePredecessorBlock(MutableBasicBlock oldBlock, MutableBasicBlock newBlock);

    Collection<ClassType> collectExceptionalSuccessorBlocks(@Nonnull MutableBasicBlock block);

    @Nonnull
    @Override
    List<MutableBasicBlock> getPredecessors();

    @Nonnull
    @Override
    List<MutableBasicBlock> getSuccessors();

    @Override
    Map<ClassType, MutableBasicBlock> getExceptionalPredecessors();

    @Nonnull
    @Override
    Map<ClassType, MutableBasicBlock> getExceptionalSuccessors();

    int getStmtCount();

    @Nonnull
    @Override
    List<Stmt> getStmts();

    @Nonnull
    @Override
    Stmt getHead();

    @Nonnull
    @Override
    Stmt getTail();

    void replaceStmt(int idx, Stmt newStmt);

}
