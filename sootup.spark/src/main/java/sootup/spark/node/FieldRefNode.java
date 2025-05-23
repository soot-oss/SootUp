package sootup.spark.node;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import sootup.core.signatures.FieldSignature;

@FieldDefaults(makeFinal=true, level= AccessLevel.PRIVATE)
@Getter
@SuperBuilder
public class FieldRefNode extends Node {

    VariableNode base;
    @NonNull
    FieldSignature field;

    @Override
    public String toString() {
        return String.format("\"%s(%s %s).%s\"", field.getType(), base.getType(), base.getName(), field.getName());
    }
}
