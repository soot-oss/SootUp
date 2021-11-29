package de.upb.swt.soot.java.core.tag;

public class SourceLnPosTag implements Tag {

    public static final String IDENTIFIER = "SourceLnPosTag";

    private final int startLn;
    private final int endLn;
    private final int startPos;
    private final int endPos;

    public SourceLnPosTag(int sline, int eline, int spos, int epos) {
        this.startLn = sline;
        this.endLn = eline;
        this.startPos = spos;
        this.endPos = epos;
    }

    public int startLn() {
        return startLn;
    }

    public int endLn() {
        return endLn;
    }

    public int startPos() {
        return startPos;
    }

    public int endPos() {
        return endPos;
    }

    @Override
    public String getName() {
        return IDENTIFIER;
    }

    @Override
    public byte[] getValue() {
        byte[] v = new byte[2];
        v[0] = (byte) (startLn / 256);
        v[1] = (byte) (startLn % 256);
        return v;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Source Line Pos Tag: ");
        sb.append("sline: ").append(startLn);
        sb.append(" eline: ").append(endLn);
        sb.append(" spos: ").append(startPos);
        sb.append(" epos: ").append(endPos);
        return sb.toString();
    }
}