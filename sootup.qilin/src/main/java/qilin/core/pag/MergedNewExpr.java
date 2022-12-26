package qilin.core.pag;

import qilin.util.DataFactory;
import sootup.core.types.ReferenceType;

import java.util.Map;

public class MergedNewExpr {
    private final ReferenceType type;
    private static final Map<ReferenceType, MergedNewExpr> map = DataFactory.createMap();

    private MergedNewExpr(ReferenceType type) {
        this.type = type;
    }

    public static MergedNewExpr v(ReferenceType type) {
        return map.computeIfAbsent(type, k -> new MergedNewExpr(type));
    }

}
