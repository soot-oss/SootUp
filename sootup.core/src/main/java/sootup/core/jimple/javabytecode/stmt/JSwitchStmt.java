package sootup.core.jimple.javabytecode.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Markus Schmidt, Thomas Johannesmeyer
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

import java.util.*;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.AbstractStmt;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.model.Body;
import sootup.core.util.printer.StmtPrinter;

/*
 * Switch Statements (combining LookupSwitch/TableSwitch)
 * @author Markus Schmidt
 */
public class JSwitchStmt extends AbstractStmt implements BranchingStmt {

  private final Immediate key;
  private List<IntConstant> values;

  private JSwitchStmt(
      boolean isTableSwitch, @NonNull StmtPositionInfo positionInfo, @NonNull Immediate key) {
    super(positionInfo);
    this.key = key;
  }

  public JSwitchStmt(
      @NonNull Immediate key, int lowIndex, int highIndex, @NonNull StmtPositionInfo positionInfo) {
    this(true, positionInfo, key);

    if (lowIndex > highIndex) {
      throw new RuntimeException(
          "Error creating switch: lowIndex("
              + lowIndex
              + ") can't be greater than highIndex("
              + highIndex
              + ").");
    }

    values = new ImmutableAscendingSequenceList(lowIndex, highIndex);
  }

  /** Constructs a new JSwitchStmt. lookupValues should be a list of IntConst s. */
  public JSwitchStmt(
      @NonNull Immediate key,
      @NonNull List<IntConstant> lookupValues,
      @NonNull StmtPositionInfo positionInfo) {
    this(false, positionInfo, key);
    values = Collections.unmodifiableList(new ArrayList<>(lookupValues));
  }

  public boolean isTableSwitch() {
    return values instanceof JSwitchStmt.ImmutableAscendingSequenceList;
  }

  @NonNull
  public Optional<Stmt> getDefaultTarget(@NonNull Body body) {
    return Optional.ofNullable(body.getBranchTargetsOf(this).get(values.size()));
  }

  public Immediate getKey() {
    return key;
  }

  @Override
  @NonNull
  public Stream<Value> getUses() {
    return Stream.concat(getKey().getUses(), Stream.of(getKey()));
  }

  @Override
  public boolean fallsThrough() {
    return false;
  }

  @Override
  public boolean branches() {
    return true;
  }

  @Override
  public <V extends StmtVisitor> V accept(@NonNull V v) {
    v.caseSwitchStmt(this);
    return v;
  }

  /** Amount of labels +1 for default handler */
  public int getValueCount() {
    return values.size() + 1;
  }

  public int getValue(int index) {
    return values.get(index).getValue();
  }

  @NonNull
  public List<IntConstant> getValues() {
    return Collections.unmodifiableList(values);
  }

  @Override
  @NonNull
  public List<Stmt> getTargetStmts(Body body) {
    return body.getBranchTargetsOf(this);
  }

  @Override
  public int getExpectedSuccessorCount() {
    return getValueCount();
  }

  @Override
  public boolean equivTo(@NonNull Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseSwitchStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return Objects.hash(getValues());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(Jimple.SWITCH)
        .append('(')
        .append(getKey())
        .append(')')
        .append(' ')
        .append('{')
        .append(" ");

    for (IntConstant value : values) {
      sb.append("    ").append(Jimple.CASE).append(' ').append(value).append(": ");
    }

    sb.append("    ").append(Jimple.DEFAULT).append(": ");
    sb.append(' ').append('}');

    return sb.toString();
  }

  @Override
  public void toString(@NonNull StmtPrinter stmtPrinter) {
    stmtPrinter.literal(Jimple.SWITCH);
    stmtPrinter.literal("(");
    getKey().toString(stmtPrinter);
    stmtPrinter.literal(")");
    stmtPrinter.newline();
    stmtPrinter.incIndent();
    stmtPrinter.handleIndent();
    stmtPrinter.literal("{");
    stmtPrinter.newline();

    final Iterable<Stmt> targets = stmtPrinter.getGraph().getBranchTargetsOf(this);
    Iterator<Stmt> targetIt = targets.iterator();
    for (IntConstant value : values) {
      stmtPrinter.handleIndent();
      stmtPrinter.literal(Jimple.CASE);
      stmtPrinter.literal(" ");
      stmtPrinter.constant(value);
      stmtPrinter.literal(": ");
      stmtPrinter.literal(Jimple.GOTO);
      stmtPrinter.literal(" ");
      stmtPrinter.stmtRef(targetIt.next(), true);
      stmtPrinter.literal(";");

      stmtPrinter.newline();
    }
    Stmt defaultTarget = targetIt.next();
    stmtPrinter.handleIndent();
    stmtPrinter.literal(Jimple.DEFAULT);
    stmtPrinter.literal(": ");
    stmtPrinter.literal(Jimple.GOTO);
    stmtPrinter.literal(" ");
    stmtPrinter.stmtRef(defaultTarget, true);
    stmtPrinter.literal(";");

    stmtPrinter.decIndent();
    stmtPrinter.newline();
    stmtPrinter.handleIndent();
    stmtPrinter.literal("}");
  }

  @NonNull
  public JSwitchStmt withKey(@NonNull Immediate key) {
    return new JSwitchStmt(key, getValues(), getPositionInfo());
  }

  @NonNull
  public JSwitchStmt withValues(@NonNull List<IntConstant> values) {
    return new JSwitchStmt(getKey(), values, getPositionInfo());
  }

  @NonNull
  public JSwitchStmt withPositionInfo(@NonNull StmtPositionInfo positionInfo) {
    return new JSwitchStmt(getKey(), getValues(), positionInfo);
  }

  /** Memory saving List<> implementation for tableswitch */
  private static class ImmutableAscendingSequenceList implements List<IntConstant> {
    private final int from;
    private final int to;

    ImmutableAscendingSequenceList(int from, int to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public int size() {
      return to - from + 1;
    }

    @Override
    public boolean isEmpty() {
      return size() <= 0;
    }

    @Override
    public boolean contains(Object o) {
      if (o instanceof IntConstant) {
        int value = ((IntConstant) o).getValue();
        return value >= from && value <= to;
      }
      return false;
    }

    @NonNull
    @Override
    public Iterator<IntConstant> iterator() {
      return listIterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
      Object[] intConstants = new IntConstant[to - from + 1];
      for (int i = 0; i < size(); i++) {
        intConstants[i] = IntConstant.getInstance(from + i);
      }
      return intConstants;
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] ts) {
      T[] intConstants = (T[]) new Object[to - from + 1];
      for (int i = 0; i < size(); i++) {
        intConstants[i] = (T) IntConstant.getInstance(from + i);
      }
      return intConstants;
    }

    @Override
    public boolean add(IntConstant constant) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
      for (Object o : collection) {
        if (!contains(o)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends IntConstant> collection) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int i, @NonNull Collection<? extends IntConstant> collection) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }

    @Override
    public IntConstant get(int i) {
      if (!(0 <= i && size() > i)) {
        throw new IndexOutOfBoundsException(
            "" + (i) + "  is out of range [ 0 , " + (size() - 1) + " ]");
      }
      return IntConstant.getInstance(from + i);
    }

    @Override
    public IntConstant set(int i, IntConstant constant) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void add(int i, IntConstant constant) {
      throw new UnsupportedOperationException();
    }

    @Override
    public IntConstant remove(int i) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
      if (!contains(o)) {
        return -1;
      }
      return ((IntConstant) o).getValue() - from;
    }

    @Override
    public int lastIndexOf(Object o) {
      // as IntConstant values are unique
      return indexOf(o);
    }

    @NonNull
    @Override
    public ListIterator<IntConstant> listIterator() {
      return listIterator(0);
    }

    @NonNull
    @Override
    public ListIterator<IntConstant> listIterator(int i) {
      return new ListIterator<IntConstant>() {
        int it = from + i - 1;

        @Override
        public boolean hasNext() {
          return it < to;
        }

        @Override
        public IntConstant next() {
          if (!hasNext()) {
            throw new IndexOutOfBoundsException("There are no more elements.");
          }
          return IntConstant.getInstance(++it);
        }

        @Override
        public boolean hasPrevious() {
          return it > from;
        }

        @Override
        public IntConstant previous() {
          if (!hasPrevious()) {
            throw new IndexOutOfBoundsException("There are no more elements.");
          }
          return IntConstant.getInstance(--it);
        }

        @Override
        public int nextIndex() {
          return it + 1;
        }

        @Override
        public int previousIndex() {
          return it - 1;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }

        @Override
        public void set(IntConstant constant) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void add(IntConstant constant) {
          throw new UnsupportedOperationException();
        }
      };
    }

    @NonNull
    @Override
    public List<IntConstant> subList(int startIdx, int endIdx) {
      return new ImmutableAscendingSequenceList(from + startIdx, from + endIdx);
    }
  }
}
