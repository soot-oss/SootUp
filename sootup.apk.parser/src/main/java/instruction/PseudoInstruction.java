package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;

public abstract class PseudoInstruction extends DexLibAbstractInstruction{
    /**
     * @param instruction the underlying dexlib instruction
     * @param codeAddress the bytecode address of this instruction
     */
    public PseudoInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }

    protected int dataFirstByte = -1;
    protected int dataLastByte = -1;
    protected int dataSize = -1;
    protected byte[] data = null;
    protected boolean loaded = false;

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public byte[] getData() {
        return data;
    }

    protected void setData(byte[] data) {
        this.data = data;
    }

    public int getDataFirstByte() {
        if (dataFirstByte == -1) {
            throw new RuntimeException("Error: dataFirstByte was not set!");
        }
        return dataFirstByte;
    }

    protected void setDataFirstByte(int dataFirstByte) {
        this.dataFirstByte = dataFirstByte;
    }

    public int getDataLastByte() {
        if (dataLastByte == -1) {
            throw new RuntimeException("Error: dataLastByte was not set!");
        }
        return dataLastByte;
    }

    protected void setDataLastByte(int dataLastByte) {
        this.dataLastByte = dataLastByte;
    }

    public int getDataSize() {
        if (dataSize == -1) {
            throw new RuntimeException("Error: dataFirstByte was not set!");
        }
        return dataSize;
    }

    protected void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public abstract void computeDataOffsets(DexBody body);
}
