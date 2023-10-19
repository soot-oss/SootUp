package tag;

import main.Tag;

public class DoubleOpTag implements Tag {
    public static final String NAME = "DoubleOpTag";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public byte[] getValue() {
        return new byte[1];
    }
}
