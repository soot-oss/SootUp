package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 *
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 *
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

import com.google.common.collect.ArrayListMultimap;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.constant.NumericConstant;
import de.upb.swt.soot.core.jimple.common.expr.AbstractConditionExpr;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.expr.JEqExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.java.bytecode.frontend.AsmUtil;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.instructions.*;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.DalvikTyper;
import de.upb.swt.soot.java.bytecode.interceptors.*;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.toolkits.scalar.*;
import org.jf.dexlib2.analysis.ClassPath;
import org.jf.dexlib2.analysis.ClassPathResolver;
import org.jf.dexlib2.analysis.ClassProvider;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.MultiDexContainer.DexEntry;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.debug.ImmutableEndLocal;
import org.jf.dexlib2.immutable.debug.ImmutableLineNumber;
import org.jf.dexlib2.immutable.debug.ImmutableRestartLocal;
import org.jf.dexlib2.immutable.debug.ImmutableStartLocal;
import org.jf.dexlib2.util.MethodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.instructions.InstructionFactory.fromInstruction;

/**
 * A DexBody contains the code of a DexMethod and is used as a wrapper around JimpleBody in the
 * jimplification process.
 *
 * @author Michael Markert
 * @author Frank Hartmann
 */
public class DexBody {
  private static final Logger logger = LoggerFactory.getLogger(DexBody.class);
  protected List<DexlibAbstractInstruction> instructions;
  // keeps track about the jimple locals that are associated with the dex
  // registers
  protected Local[] registerLocals;
  protected Local storeResultLocal;
  protected Map<Integer, DexlibAbstractInstruction> instructionAtAddress;

  protected List<DeferableInstruction> deferredInstructions;
  protected Set<RetypeableInstruction> instructionsToRetype;
  protected DanglingInstruction dangling;

  protected int numRegisters;
  protected int numParameterRegisters;
  protected final List<Type> parameterTypes;
  protected final List<String> parameterNames;
  protected boolean isStatic;

  protected Body.BodyBuilder bodyBuilder;
  protected List<? extends TryBlock<? extends ExceptionHandler>> tries;

  protected ClassType declaringClassType;

  protected final DexEntry<? extends DexFile> dexEntry;
  protected final Method method;

  private boolean use_original_names = true;
  private boolean stabilize_local_names = true;
  private int options_wrong_staticness = 0;
  private int options_wrong_staticness_fix = 0;
  private int options_wrong_staticness_fixstrict = 0;

  /**
   * An entry of debug information for a register from the dex file.
   *
   * @author Zhenghao Hu
   */
  protected class RegDbgEntry {
    public int startAddress;
    public int endAddress;
    public int register;
    public String name;
    public Type type;
    public String signature;

    public RegDbgEntry(int sa, int ea, int reg, String nam, String ty, String sig) {
      this.startAddress = sa;
      this.endAddress = ea;
      this.register = reg;
      this.name = nam;
      this.type = DexType.toSoot(ty);
      this.signature = sig;
    }
  }

  private final ArrayListMultimap<Integer, RegDbgEntry> localDebugs;

  // detect array/instructions overlapping obfuscation
  protected List<PseudoInstruction> pseudoInstructionData = new ArrayList<PseudoInstruction>();

  PseudoInstruction isAddressInData(int a) {
    for (PseudoInstruction pi : pseudoInstructionData) {
      int fb = pi.getDataFirstByte();
      int lb = pi.getDataLastByte();
      if (fb <= a && a <= lb) {
        return pi;
      }
    }
    return null;
  }

  // the set of names used by Jimple locals
  protected Set<String> takenLocalNames;

  /**
   * Allocate a fresh name for Jimple local
   *
   * @param hint A name that the fresh name will look like
   * @author Zhixuan Yang (yangzhixuan@sbrella.com)
   */
  protected String freshLocalName(String hint) {
    if (hint == null || hint.equals("")) {
      hint = "$local";
    }
    String fresh;
    if (!takenLocalNames.contains(hint)) {
      fresh = hint;
    } else {
      for (int i = 1; ; i++) {
        fresh = hint + Integer.toString(i);
        if (!takenLocalNames.contains(fresh)) {
          break;
        }
      }
    }
    takenLocalNames.add(fresh);
    return fresh;
  }

  /**
   * @param dexFile the codeitem that is contained in this body
   * @param method the method that is associated with this body
   */
  protected DexBody(
      DexEntry<? extends DexFile> dexFile, Method method, ClassType declaringClassType) {
    MethodImplementation code = method.getImplementation();
    if (code == null) {
      throw new RuntimeException("error: no code for method " + method.getName());
    }
    this.declaringClassType = declaringClassType;
    tries = code.getTryBlocks();

    List<? extends MethodParameter> parameters = method.getParameters();
    if (parameters != null) {
      parameterNames = new ArrayList<String>();
      parameterTypes = new ArrayList<Type>();
      for (MethodParameter param : method.getParameters()) {
        parameterNames.add(param.getName());
        parameterTypes.add(DexType.toSoot(param.getType()));
      }
    } else {
      parameterNames = Collections.emptyList();
      parameterTypes = Collections.emptyList();
    }

    // [ms]: performance optimization: don't create all modifiers - just check for static
    isStatic = Modifier.isStatic(AsmUtil.getModifiers(method.getAccessFlags()));//isStatic(method.getAccessFlags());
    numRegisters = code.getRegisterCount();
    numParameterRegisters = MethodUtil.getParameterRegisterCount(method);
    if (!isStatic) {
      numParameterRegisters--;
    }

    instructions = new ArrayList<DexlibAbstractInstruction>();
    instructionAtAddress = new HashMap<Integer, DexlibAbstractInstruction>();
    localDebugs = ArrayListMultimap.create();
    takenLocalNames = new HashSet<String>();

    registerLocals = new Local[numRegisters];

    extractDexInstructions(code);

    // Check taken from Android's dalvik/libdex/DexSwapVerify.cpp
    if (numParameterRegisters > numRegisters) {
      throw new RuntimeException(
          "Malformed dex file: insSize ("
              + numParameterRegisters
              + ") > registersSize ("
              + numRegisters
              + ")");
    }

    for (DebugItem di : code.getDebugItems()) {
      if (di instanceof ImmutableLineNumber) {
        ImmutableLineNumber ln = (ImmutableLineNumber) di;
        DexlibAbstractInstruction ins = instructionAtAddress(ln.getCodeAddress());
        if (ins == null) {
          // Debug.printDbg("Line number tag pointing to invalid
          // offset: " + ln.getCodeAddress());
          continue;
        }
        ins.setLineNumber(ln.getLineNumber());
      } else if (di instanceof ImmutableStartLocal || di instanceof ImmutableRestartLocal) {
        int reg, codeAddr;
        String type, signature, name;
        if (di instanceof ImmutableStartLocal) {
          ImmutableStartLocal sl = (ImmutableStartLocal) di;
          reg = sl.getRegister();
          codeAddr = sl.getCodeAddress();
          name = sl.getName();
          type = sl.getType();
          signature = sl.getSignature();
        } else {
          ImmutableRestartLocal sl = (ImmutableRestartLocal) di;
          // ImmutableRestartLocal and ImmutableStartLocal share the same members but
          // don't share a base. So we have to write some duplicated code.
          reg = sl.getRegister();
          codeAddr = sl.getCodeAddress();
          name = sl.getName();
          type = sl.getType();
          signature = sl.getSignature();
        }
        if (name != null && type != null) {
          localDebugs.put(
              reg, new RegDbgEntry(codeAddr, -1 /* endAddress */, reg, name, type, signature));
        }
      } else if (di instanceof ImmutableEndLocal) {
        ImmutableEndLocal el = (ImmutableEndLocal) di;
        List<RegDbgEntry> lds = localDebugs.get(el.getRegister());
        if (lds == null || lds.isEmpty()) {
          // Invalid debug info
          continue;
        } else {
          lds.get(lds.size() - 1).endAddress = el.getCodeAddress();
        }
      }
    }

    this.dexEntry = dexFile;
    this.method = method;
  }

  /**
   * Extracts the list of dalvik instructions from dexlib and converts them into our own instruction
   * data model
   *
   * @param code The dexlib method implementation
   */
  protected void extractDexInstructions(MethodImplementation code) {
    int address = 0;
    for (Instruction instruction : code.getInstructions()) {
      DexlibAbstractInstruction dexInstruction = fromInstruction(instruction, address);
      instructions.add(dexInstruction);
      instructionAtAddress.put(address, dexInstruction);
      address += instruction.getCodeUnits();
    }
  }

  /** Return the types that are used in this body. */
  public Set<Type> usedTypes() {
    Set<Type> types = new HashSet<Type>();
    for (DexlibAbstractInstruction i : instructions) {
      types.addAll(i.introducedTypes());
    }

    if (tries != null) {
      for (TryBlock<? extends ExceptionHandler> tryItem : tries) {
        List<? extends ExceptionHandler> hList = tryItem.getExceptionHandlers();
        for (ExceptionHandler handler : hList) {
          String exType = handler.getExceptionType();
          if (exType == null) {
            // Exceptions
            continue;
          }
          types.add(DexType.toSoot(exType));
        }
      }
    }

    return types;
  }

  /**
   * Add unit to this body.
   *
   * @param u Unit to add.
   */
  public void add(Stmt u) {
    getBody().getStmts().add(u);
  }

  /**
   * Add a deferred instruction to this body.
   *
   * @param i the deferred instruction.
   */
  public void addDeferredJimplification(DeferableInstruction i) {
    deferredInstructions.add(i);
  }

  /**
   * Add a retypeable instruction to this body.
   *
   * @param i the retypeable instruction.
   */
  public void addRetype(RetypeableInstruction i) {
    instructionsToRetype.add(i);
  }

  /**
   * Return the associated JimpleBody.
   *
   * @throws RuntimeException if no jimplification happened yet.
   */
  public Body getBody() {
    if (bodyBuilder == null) {
      throw new RuntimeException("No jimplification happened yet, no body available.");
    }
    return bodyBuilder.build();
  }

  /** Return the Locals that are associated with the current register state. */
  public Local[] getRegisterLocals() {
    return registerLocals;
  }

  /**
   * Return the Local that are associated with the number in the current register state.
   *
   * <p>Handles if the register number actually points to a method parameter.
   *
   * @param num the register number
   * @throws InvalidDalvikBytecodeException
   */
  public Local getRegisterLocal(int num) throws InvalidDalvikBytecodeException {
    int totalRegisters = registerLocals.length;
    if (num > totalRegisters) {
      throw new InvalidDalvikBytecodeException(
          "Trying to access register "
              + num
              + " but only "
              + totalRegisters
              + " is/are available.");
    }
    return registerLocals[num];
  }

  public Local getStoreResultLocal() {
    return storeResultLocal;
  }

  /**
   * Return the instruction that is present at the byte code address.
   *
   * @param address the byte code address.
   * @throws RuntimeException if address is not part of this body.
   */
  public DexlibAbstractInstruction instructionAtAddress(int address) {
    DexlibAbstractInstruction i = null;
    while (i == null && address >= 0) {
      // catch addresses can be in the middlde of last instruction. Ex. in
      // com.letang.ldzja.en.apk:
      //
      // 042c46: 7020 2a15 0100 |008f: invoke-direct {v1, v0},
      // Ljavax/mi...
      // 042c4c: 2701 |0092: throw v1
      // catches : 4
      // <any> -> 0x0065
      // 0x0069 - 0x0093
      //
      // SA, 14.05.2014: We originally scanned only two code units back.
      // This is not sufficient
      // if we e.g., have a wide constant and the line number in the debug
      // sections points to
      // some address the middle.
      i = instructionAtAddress.get(address);
      address--;
    }
    return i;
  }


  /**
   * Return the jimple equivalent of this body.
   *
   * @param m the SootMethod that contains this body
   */
  public Body jimplify(Body b, SootMethod m) {

    final Jimple jimple = JavaJimple.getInstance();
    final UnknownType unknownType = UnknownType.getInstance();
    final NullConstant nullConstant = NullConstant.getInstance();

    // final Options options = Options.v();

    /*
     * Timer t_whole_jimplification = new Timer(); Timer t_num = new Timer(); Timer t_null = new Timer();
     *
     * t_whole_jimplification.start();
     */

    //JBOptions jbOptions = new JBOptions(PhaseOptions.v().getPhaseOptions("jb"));
    bodyBuilder = Body.builder();
    deferredInstructions = new ArrayList<DeferableInstruction>();
    instructionsToRetype = new HashSet<RetypeableInstruction>();

    if (use_original_names) {
      // FIXME - what is alternative to PhaseOptions - commented line below
      // PhaseOptions.v().setPhaseOptionIfUnset("jb.lns", "only-stack-locals");
    }
    if (stabilize_local_names) {
      // FIXME - what is alternative to PhaseOptions - commented line below
      // PhaseOptions.v().setPhaseOption("jb.lns", "sort-locals:true");
    }

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      DalvikTyper.v().clear();
    }

    // process method parameters and generate Jimple locals from Dalvik
    // registers
    List<Local> paramLocals = new LinkedList<Local>();
    if (!isStatic) {
      int thisRegister = numRegisters - numParameterRegisters - 1;

      Local thisLocal =
          jimple.newLocal(freshLocalName("this"), unknownType); // generateLocal(UnknownType.v());
      bodyBuilder.getLocals().add(thisLocal);

      registerLocals[thisRegister] = thisLocal;
      JIdentityStmt idStmt =
          (JIdentityStmt) jimple.newIdentityStmt(thisLocal, jimple.newThisRef(declaringClassType), StmtPositionInfo.createNoStmtPositionInfo());
      add(idStmt);
      paramLocals.add(thisLocal);
      if (IDalvikTyper.ENABLE_DVKTYPER) {
        DalvikTyper.v()
            .setType(idStmt.getLeftOp(), bodyBuilder.getMethodSignature().getDeclClassType(), false);
      }
    }
    {
      int i = 0; // index of parameter type
      int argIdx = 0;
      int parameterRegister = numRegisters - numParameterRegisters; // index of parameter register
      for (Type t : parameterTypes) {

        String localName = null;
        Type localType = null;
        if (use_original_names) {
          // Attempt to read original parameter name.
          try {
            localName = parameterNames.get(argIdx);
            localType = parameterTypes.get(argIdx);
          } catch (Exception ex) {
            logger.error("Exception while reading original parameter names.", ex);
          }
        }
        if (localName == null && localDebugs.containsKey(parameterRegister)) {
          localName = localDebugs.get(parameterRegister).get(0).name;
        } else {
          localName = "$u" + parameterRegister;
        }
        if (localType == null) {
          // may only use UnknownType here because the local may be
          // reused with a different type later (before splitting)
          localType = unknownType;
        }

        Local gen = jimple.newLocal(freshLocalName(localName), localType);
        bodyBuilder.getLocals().add(gen);
        registerLocals[parameterRegister] = gen;

        JIdentityStmt idStmt =
                jimple.newIdentityStmt(gen, jimple.newParameterRef(t, i++), StmtPositionInfo.createNoStmtPositionInfo());
        add(idStmt);
        paramLocals.add(gen);
        if (IDalvikTyper.ENABLE_DVKTYPER) {
          DalvikTyper.v().setType(idStmt.getLeftOp(), t, false);
        }

        // some parameters may be encoded on two registers.
        // in Jimple only the first Dalvik register name is used
        // as the corresponding Jimple Local name. However, we also add
        // the second register to the registerLocals array since it
        // could be used later in the Dalvik bytecode
        if (t instanceof PrimitiveType.LongType || t instanceof PrimitiveType.DoubleType) {
          parameterRegister++;
          // may only use UnknownType here because the local may be reused with a different
          // type later (before splitting)
          String name;
          if (localDebugs.containsKey(parameterRegister)) {
            name = localDebugs.get(parameterRegister).get(0).name;
          } else {
            name = "$u" + parameterRegister;
          }
          Local g = jimple.newLocal(freshLocalName(name), unknownType);
          bodyBuilder.getLocals().add(g);
          registerLocals[parameterRegister] = g;
        }

        parameterRegister++;
        argIdx++;
      }
    }

    for (int i = 0; i < (numRegisters - numParameterRegisters - (isStatic ? 0 : 1)); i++) {
      String name;
      if (localDebugs.containsKey(i)) {
        name = localDebugs.get(i).get(0).name;
      } else {
        name = "$u" + i;
      }
      registerLocals[i] = jimple.newLocal(freshLocalName(name), unknownType);
      bodyBuilder.getLocals().add(registerLocals[i]);
    }

    // add local to store intermediate results
    storeResultLocal = jimple.newLocal(freshLocalName("$u-1"), unknownType);
    bodyBuilder.getLocals().add(storeResultLocal);

    // process bytecode instructions
    final DexFile dexFile = dexEntry.getDexFile();
    final boolean isOdex =
        dexFile instanceof DexBackedDexFile
            ? ((DexBackedDexFile) dexFile).supportsOptimizedOpcodes()
            : false;

    ClassPath cp = null;
    if (isOdex) {
      // FIXME - implement new way to get separated ClassPath
      String[] sootClasspath = options.soot_classpath().split(File.pathSeparator);
      List<String> classpathList = new ArrayList<String>();
      for (String str : sootClasspath) {
        classpathList.add(str);
      }
      try {
        ClassPathResolver resolver =
            new ClassPathResolver(classpathList, classpathList, classpathList, dexEntry);
        cp = new ClassPath(resolver.getResolvedClassProviders().toArray(new ClassProvider[0]));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    int prevLineNumber = -1;
    for (DexlibAbstractInstruction instruction : instructions) {
      if (isOdex && instruction instanceof OdexInstruction) {
        ((OdexInstruction) instruction).deOdex(dexFile, method, cp);
      }
      if (dangling != null) {
        dangling.finalize(this, instruction);
        dangling = null;
      }
      instruction.jimplify(this);
      if (instruction.getLineNumber() > 0) {
        prevLineNumber = instruction.getLineNumber();
      } else {
        instruction.setLineNumber(prevLineNumber);
      }
    }
    if (dangling != null) {
      dangling.finalize(this, null);
    }
    for (DeferableInstruction instruction : deferredInstructions) {
      instruction.deferredJimplify(this);
    }

    if (tries != null) {
      addTraps();
    }

    /*if (options.keep_line_number()) {
      fixLineNumbers();
    }*/

    // At this point Jimple code is generated
    // Cleaning...

    instructions = null;
    // registerLocals = null;
    // storeResultLocal = null;
    instructionAtAddress.clear();
    // localGenerator = null;
    deferredInstructions = null;
    // instructionsToRetype = null;
    dangling = null;
    tries = null;
    parameterNames.clear();

    /*
     * We eliminate dead code. Dead code has been shown to occur under the following circumstances.
     *
     * 0006ec: 0d00 |00a2: move-exception v0 ... 0006f2: 0d00 |00a5: move-exception v0 ... 0x0041 - 0x008a
     * Ljava/lang/Throwable; -> 0x00a5 <any> -> 0x00a2
     *
     * Here there are two traps both over the same region. But the same always fires, hence rendering the code at a2
     * unreachable. Dead code yields problems during local splitting because locals within dead code will not be split. Hence
     * we remove all dead code here.
     */

    // Fix traps that do not catch exceptions
    new DexTrapStackFixer().interceptBody(bodyBuilder);

    // Sort out jump chains
    new DexJumpChainShortener().interceptBody(bodyBuilder);

    // Make sure that we don't have any overlapping uses due to returns
    new DexReturnInliner().interceptBody(bodyBuilder);

    // Shortcut: Reduce array initializations
    new DexArrayInitReducer().interceptBody(bodyBuilder);

    // split first to find undefined uses
    getLocalSplitter().interceptBody(bodyBuilder);

    // Remove dead code and the corresponding locals before assigning types
    getUnreachableCodeEliminator().interceptBody(bodyBuilder);
    new DeadAssignmentEliminator().interceptBody(bodyBuilder);
    new UnusedLocalEliminator().interceptBody(bodyBuilder);


    for (RetypeableInstruction i : instructionsToRetype) {
      i.retype(bodyBuilder);
    }

    // {
    // // remove instructions from instructions list
    // List<DexlibAbstractInstruction> iToRemove = new
    // ArrayList<DexlibAbstractInstruction>();
    // for (DexlibAbstractInstruction i: instructions)
    // if (!jBody.getUnits().contains(i.getUnit()))
    // iToRemove.add(i);
    // for (DexlibAbstractInstruction i: iToRemove) {
    // Debug.printDbg("removing dexinstruction containing unit '",
    // i.getUnit() ,"'");
    // instructions.remove(i);
    // }
    // }

    if (IDalvikTyper.ENABLE_DVKTYPER) {

      new DexReturnValuePropagator().interceptBody(this.bodyBuilder);
      getCopyPopagator().interceptBody(this.bodyBuilder);
      new DexNullThrowTransformer().interceptBody(this.bodyBuilder);
      DalvikTyper.v().typeUntypedConstrantInDiv(this.bodyBuilder);
      new DeadAssignmentEliminator().interceptBody(this.bodyBuilder);
      new UnusedLocalEliminator().interceptBody(this.bodyBuilder);

      DalvikTyper.v().assignType(this.bodyBuilder);
      // jBody.validate();
      Body body = this.bodyBuilder.build();
      body.validateUses();
      body.validateValues();
      // jBody.checkInit();
      // Validate.validateArrays(jBody);
      // jBody.checkTypes();
      // jBody.checkLocals();

    } else {
      // t_num.start();
      new DexNumTransformer().interceptBody(this.bodyBuilder);
      // t_num.end();

      new DexReturnValuePropagator().interceptBody(this.bodyBuilder);
      getCopyPopagator().interceptBody(this.bodyBuilder);

      new DexNullThrowTransformer().interceptBody(this.bodyBuilder);

      // t_null.start();
      new DexNullTransformer().interceptBody(this.bodyBuilder);
      // t_null.end();

      new DexIfTransformer().interceptBody(this.bodyBuilder);

      new DeadAssignmentEliminator().interceptBody(this.bodyBuilder);
      new UnusedLocalEliminator().interceptBody(this.bodyBuilder);

      // DexRefsChecker.v().transform(jBody);
      new DexNullArrayRefTransformer().interceptBody(this.bodyBuilder);
    }

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      for (Local l : this.bodyBuilder.getLocals()) {
        l.getType();// setType(unknownType);
      }
    }

    // Remove "instanceof" checks on the null constant
    new DexNullInstanceofTransformer().interceptBody(this.bodyBuilder);

    new TypeAssigner().interceptBody(this.bodyBuilder);

    final ReferenceType objectType = JavaIdentifierFactory.getInstance().getClassType("java.lang.Object");
    if (IDalvikTyper.ENABLE_DVKTYPER) {
      for (Stmt u : this.bodyBuilder.getStmtGraph().nodes()) {
        if (u instanceof JIfStmt) {
          AbstractConditionExpr expr = ((JIfStmt) u).getCondition();
          if (((expr instanceof JEqExpr) || (expr instanceof JNeExpr))) {
            Value op1 = expr.getOp1();
            Value op2 = expr.getOp2();
            if (op1 instanceof Constant && op2 instanceof Local) {
              Local l = (Local) op2;
              Type ltype = l.getType();
              if (ltype instanceof PrimitiveType) {
                continue;
              }
              if (!(op1 instanceof IntConstant)) {
                // null is
                // IntConstant(0)
                // in Dalvik
                continue;
              }
              IntConstant icst = (IntConstant) op1;
              int val = icst.getValue();
              if (val != 0) {
                continue;
              }
              expr = expr.withOp1(nullConstant);
            } else if (op1 instanceof Local && op2 instanceof Constant) {
              Local l = (Local) op1;
              Type ltype = l.getType();
              if (ltype instanceof PrimitiveType) {
                continue;
              }
              if (!(op2 instanceof IntConstant)) {
                // null is
                // IntConstant(0)
                // in Dalvik
                continue;
              }
              IntConstant icst = (IntConstant) op2;
              int val = icst.getValue();
              if (val != 0) {
                continue;
              }
              expr = expr.withOp2(nullConstant);
            } else if (op1 instanceof Local && op2 instanceof Local) {
              // nothing to do
            } else if (op1 instanceof Constant && op2 instanceof Constant) {

              if (op1 instanceof NullConstant && op2 instanceof NumericConstant) {
                IntConstant nc = (IntConstant) op2;
                if (nc.getValue() != 0) {
                  throw new RuntimeException("expected value 0 for int constant. Got " + expr);
                }
                expr = expr.withOp2(NullConstant.getInstance());
              } else if (op2 instanceof NullConstant && op1 instanceof NumericConstant) {
                IntConstant nc = (IntConstant) op1;
                if (nc.getValue() != 0) {
                  throw new RuntimeException("expected value 0 for int constant. Got " + expr);
                }

                expr = expr.withOp1(nullConstant);
              }



            } else {
              throw new RuntimeException("error: do not handle if: " + u);
            }
            JIfStmt ifStmt = new JIfStmt(expr, u.getPositionInfo());
            this.bodyBuilder.replaceStmt(u, ifStmt);
          }
        }
      }

      // For null_type locals: replace their use by NullConstant()
      Collection<Value> uses = this.bodyBuilder.getUses();
      // List<ValueBox> defs = jBody.getDefBoxes();
      List<Value> toNullConstantify = new ArrayList<Value>();
      List<Local> toRemove = new ArrayList<Local>();
      for (Local l : this.bodyBuilder.getLocals()) {

        if (l.getType() instanceof NullType) {
          toRemove.add(l);
          for (Value v : uses) {
            if (v == l) {
              toNullConstantify.add(v);
            }
          }
        }
      }
      // FIXME reflect it in the statements Value vb valuebox to both
      for (Value vb : toNullConstantify) {
        System.out.println("replace valuebox '" + vb + " with null constant");
        vb.setValue(nullConstant);
      }
      // FIXME reflect it in the statements Value vb valuebox to both
      for (Local l : toRemove) {
        System.out.println("removing null_type local " + l);
        l.setType(objectType);
      }
    }

    // We pack locals that are not used in overlapping regions. This may
    // again lead to unused locals which we have to remove.
    new LocalPacker().interceptBody(this.bodyBuilder);
    new UnusedLocalEliminator().interceptBody(this.bodyBuilder);
    new LocalNameStandardizer().interceptBody(this.bodyBuilder);

    // Some apps reference static fields as instance fields. We fix this
    // on the fly.

    if (options_wrong_staticness == options_wrong_staticness_fix
        || options_wrong_staticness == options_wrong_staticness_fixstrict) {
      new FieldStaticnessCorrector(view).interceptBody(this.bodyBuilder);
      new MethodStaticnessCorrector().interceptBody(this.bodyBuilder);
    }

    // Inline PackManager.v().getPack("jb").apply(jBody);
    // Keep only transformations that have not been done
    // at this point.
    new TrapTightener().interceptBody(this.bodyBuilder);
    TrapMinimizer.v().interceptBody(this.bodyBuilder);
    // LocalSplitter.v().transform(jBody);
    new Aggregator().interceptBody(this.bodyBuilder);
    // new UnusedLocalEliminator().v().transform(jBody);
    // TypeAssigner.v().transform(jBody);
    // LocalPacker.v().transform(jBody);
    // LocalNameStandardizer.v().transform(jBody);

    // Remove if (null == null) goto x else <madness>. We can only do this
    // after we have run the constant propagation as we might not be able
    // to statically decide the conditions earlier.
    new ConditionalBranchFolder().interceptBody(this.bodyBuilder);

    // FIXME - missing classes from old soot in toolkit package
    // Remove unnecessary typecasts
    new ConstantCastEliminator().interceptBody(this.bodyBuilder);
    new IdentityCastEliminator().interceptBody(this.bodyBuilder);

    // Remove unnecessary logic operations
    new IdentityOperationEliminator().interceptBody(this.bodyBuilder);

    // We need to run this transformer since the conditional branch folder
    // might have rendered some code unreachable (well, it was unreachable
    // before as well, but we didn't know).
    new UnreachableCodeEliminator().interceptBody(this.bodyBuilder);

    // Not sure whether we need this even though we do it earlier on as
    // the earlier pass does not have type information
    // CopyPropagator.v().transform(jBody);

    // we might have gotten new dead assignments and unused locals through
    // copy propagation and unreachable code elimination, so we have to do
    // this again
    new DeadAssignmentEliminator().interceptBody(this.bodyBuilder);
    new UnusedLocalEliminator().interceptBody(this.bodyBuilder);
    new NopEliminator().interceptBody(this.bodyBuilder);

    // Remove unnecessary chains of return statements
    new DexReturnPacker().interceptBody(this.bodyBuilder);

    for (Stmt u : this.bodyBuilder.getStmts()) {
      if (u instanceof JAssignStmt) {
        JAssignStmt ass = (JAssignStmt) u;
        if (ass.getRightOp() instanceof JCastExpr) {
          JCastExpr c = (JCastExpr) ass.getRightOp();
          if (c.getType() instanceof NullType) {
            // FIXME as I did in other abstract class
            ass = ass.withLeftOp(nullConstant);
          }
        }
      }
      if (u instanceof AbstractDefinitionStmt) {
        AbstractDefinitionStmt def = (AbstractDefinitionStmt) u;
        // If the body references a phantom class in a
        // CaughtExceptionRef,
        // we must manually fix the hierarchy
        if (def.getLeftOp() instanceof Local && def.getRightOp() instanceof JCaughtExceptionRef) {
          Type t = def.getLeftOp().getType();
          if (t instanceof ClassType) {
            ClassType rt = (ClassType) t;
            // FIXME soot class needs to resolved via view
            //if (rt.getSootClass().isPhantom()
              //  && !rt.getSootClass().hasSuperclass()
              //  && !rt.toString().equals("java.lang.Throwable")) {
             // rt.getSootClass().setSuperclass(Scene.v().getSootClass("java.lang.Throwable"));
            // }
          }
        }
      }
    }

    // Replace local type null_type by java.lang.Object.
    //
    // The typing engine cannot find correct type for such code:
    //
    // null_type $n0;
    // $n0 = null;
    // $r4 = virtualinvoke $n0.<java.lang.ref.WeakReference:
    // java.lang.Object get()>();
    //
    // FIXME reflect it in the statements Value vb valuebox
    for (Local l : this.bodyBuilder.getLocals()) {
      Type t = l.getType();
      if (t instanceof NullType) {
        l.withType(objectType);
      }
    }

    // Must be last to ensure local ordering does not change
    new LocalNameStandardizer().interceptBody(this.bodyBuilder);

    // t_whole_jimplification.end();

    return this.bodyBuilder.build();
  }

  /**
   * Fixes the line numbers. If there is a unit without a line number, it gets the line number of
   * the last (transitive) predecessor that has a line number.
   */
  protected void fixLineNumbers() {
   /* int prevLn = -1;
    for (DexlibAbstractInstruction instruction : instructions) {
      Unit unit = instruction.getUnit();
      int lineNumber = unit.getJavaSourceStartLineNumber();
      if (lineNumber < 0) {
        if (prevLn >= 0) {
          unit.addTag(new LineNumberTag(prevLn));
          unit.addTag(new SourceLineNumberTag(prevLn));
        }
      } else {
        prevLn = lineNumber;
      }
    }*/
  }

  private LocalSplitter localSplitter = null;

  protected LocalSplitter getLocalSplitter() {
    if (this.localSplitter == null) {
      this.localSplitter = new LocalSplitter();
    }
    return this.localSplitter;
  }

  private UnreachableCodeEliminator unreachableCodeEliminator = null;

  protected UnreachableCodeEliminator getUnreachableCodeEliminator() {
    if (this.unreachableCodeEliminator == null) {
      this.unreachableCodeEliminator = new UnreachableCodeEliminator();
    }
    return this.unreachableCodeEliminator;
  }

  private CopyPropagator copyPropagator = null;

  protected CopyPropagator getCopyPopagator() {
    if (this.copyPropagator == null) {
      this.copyPropagator = new CopyPropagator();
    }
    return this.copyPropagator;
  }

  /** Set a dangling instruction for this body. */
  public void setDanglingInstruction(DanglingInstruction i) {
    dangling = i;
  }

  /**
   * Return the instructions that appear (lexically) after the given instruction.
   *
   * @param instruction the instruction which successors will be returned.
   */
  public List<DexlibAbstractInstruction> instructionsAfter(DexlibAbstractInstruction instruction) {
    int i = instructions.indexOf(instruction);
    if (i == -1) {
      throw new IllegalArgumentException("Instruction" + instruction + "not part of this body.");
    }

    return instructions.subList(i + 1, instructions.size());
  }

  /**
   * Return the instructions that appear (lexically) before the given instruction.
   *
   * <p>The instruction immediately before the given is the first instruction and so on.
   *
   * @param instruction the instruction which successors will be returned.
   */
  public List<DexlibAbstractInstruction> instructionsBefore(DexlibAbstractInstruction instruction) {
    int i = instructions.indexOf(instruction);
    if (i == -1) {
      throw new IllegalArgumentException("Instruction " + instruction + " not part of this body.");
    }

    List<DexlibAbstractInstruction> l = new ArrayList<DexlibAbstractInstruction>();
    l.addAll(instructions.subList(0, i));
    Collections.reverse(l);
    return l;
  }

  /**
   * Add the traps of this body.
   *
   * <p>Should only be called at the end jimplify.
   */
  private void addTraps() {
    for (TryBlock<? extends ExceptionHandler> tryItem : tries) {
      int startAddress = tryItem.getStartCodeAddress();
      int length = tryItem.getCodeUnitCount(); // .getTryLength();
      int endAddress = startAddress + length; // - 1;
      Stmt beginStmt = instructionAtAddress(startAddress).getStmt();
      // (startAddress + length) typically points to the first byte of the
      // first instruction after the try block
      // except if there is no instruction after the try block in which
      // case it points to the last byte of the last
      // instruction of the try block. Removing 1 from (startAddress +
      // length) always points to "somewhere" in
      // the last instruction of the try block since the smallest
      // instruction is on two bytes (nop = 0x0000).
      Stmt endStmt = instructionAtAddress(endAddress).getStmt();
      // if the try block ends on the last instruction of the body, add a
      // nop instruction so Soot can include
      // the last instruction in the try block.
      if (bodyBuilder.getStmts().get(bodyBuilder.getStmts().size()-1) == endStmt
          && instructionAtAddress(endAddress - 1).getStmt() == endStmt) {
        Stmt nop = Jimple.newNopStmt(endStmt.getPositionInfo());
        // FIXME - insertafter needs to be resolved
        bodyBuilder.getStmts().insertAfter(nop, endStmt);
        endStmt = nop;
      }

      List<? extends ExceptionHandler> hList = tryItem.getExceptionHandlers();
      for (ExceptionHandler handler : hList) {
        String exceptionType = handler.getExceptionType();
        if (exceptionType == null) {
          exceptionType = "java/lang/Throwable;";
        }
        Type t = DexType.toSoot(exceptionType);
        // exceptions can only be of RefType
        if (t instanceof ReferenceType) {
          // FIXME - SootClass needs to resolved
          ClassType exception = ((ClassType) t);
          DexlibAbstractInstruction instruction =
              instructionAtAddress(handler.getHandlerCodeAddress());
          if (!(instruction instanceof MoveExceptionInstruction)) {
            logger.debug(
                ""
                    + String.format(
                        "First instruction of trap handler unit not MoveException but %s",
                        instruction.getClass().getName()));
          } else {
            ((MoveExceptionInstruction) instruction).setRealType(this, exception);
          }

          Trap trap = Jimple.newTrap(exception, beginStmt, endStmt, instruction.getStmt());
          bodyBuilder.getTraps().add(trap);
        }
      }
    }
  }
}
