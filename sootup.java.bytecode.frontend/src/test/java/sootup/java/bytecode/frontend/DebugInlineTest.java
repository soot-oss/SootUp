package sootup.java.bytecode.frontend;

import java.nio.file.Paths;
import java.util.*;
import java.lang.reflect.*;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class DebugInlineTest {

  @Test
  public void test() throws Exception {
    // Read the class file with ASM directly
    java.io.InputStream is = new java.io.FileInputStream("src/test/resources/soot-1577/g.class");
    ClassReader cr = new ClassReader(is);
    ClassNode cn = new ClassNode();
    cr.accept(cn, ClassReader.SKIP_FRAMES);
    is.close();

    for (MethodNode mn : cn.methods) {
      if (mn.name.equals("h")) {
        System.out.println("=== Method h ===");
        System.out.println("Trap handlers:");
        Set<LabelNode> handlerLabels = new LinkedHashSet<>();
        for (TryCatchBlockNode tc : mn.tryCatchBlocks) {
          handlerLabels.add(tc.handler);
          System.out.println("  handler=" + System.identityHashCode(tc.handler) + 
              " start=" + System.identityHashCode(tc.start) + 
              " end=" + System.identityHashCode(tc.end) + " type=" + tc.type);
        }
        
        System.out.println("\nJump instructions targeting handler labels:");
        for (AbstractInsnNode insn : mn.instructions) {
          if (insn instanceof JumpInsnNode) {
            JumpInsnNode jmp = (JumpInsnNode) insn;
            LabelNode target = jmp.label;
            boolean isHandler = handlerLabels.contains(target);
            if (isHandler || jmp.getOpcode() == Opcodes.GOTO) {
              System.out.println("  opcode=" + jmp.getOpcode() + 
                  " target=" + System.identityHashCode(target) + 
                  " isHandler=" + isHandler);
            }
          }
        }
        
        System.out.println("\nAll labels in instruction list:");
        int idx = 0;
        for (AbstractInsnNode insn : mn.instructions) {
          if (insn instanceof LabelNode) {
            LabelNode ln = (LabelNode) insn;
            boolean isHandler = handlerLabels.contains(ln);
            System.out.println("  idx=" + idx + " label=" + System.identityHashCode(ln) + " isHandler=" + isHandler);
          }
          idx++;
        }
      }
    }
  }
}
